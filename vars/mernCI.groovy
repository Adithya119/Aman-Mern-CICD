def call (Map config = [:]) {

  def appDirpath = config.appDirpath            // very important because we are parsing thesevariables inside the stages {} block, unlike config.repoName which is used outside of stages block. The stages look for globally defined config maps I guess.
  def k8sDirpath = config.k8sDirpath
  def sonarProjectname = config.sonarProjectname
  def sonarProjectkey = config.sonarProjectkey

  pipeline {
    agent any 
    
    tools {
        nodejs 'nodejs'
    }
    
    environment  {
        SCANNER_HOME=tool 'sonar-scanner'
        AWS_ACCOUNT_ID = credentials('AWS_ACC_ID')
        AWS_ECR_REPO_NAME = credentials("${config.repoName}")     // variable
        AWS_DEFAULT_REGION = 'ap-south-1'
        REPOSITORY_URI = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com/"
    }

    stages {

        stage('Cleaning Workspace') {
            steps {
                cleanWs()
            }
        }

        stage('Checkout from Git') {
            steps {
                git url: 'https://github.com/Adithya119/Aman-Mern-CICD.git'
            }
        }

        stage('Sonarqube Analysis') {
            steps {
                script {
                  dir("${appDirpath}") {                    // variable   // use Double-Quoted Strings inside dir() because single quotes was not reading the value of ${appDirpath}.
                    withSonarQubeEnv('sonarqube server') {                 // use Double-Quoted Strings (""") with sh in this case because inside the shell, you are using double-quotes for "${sonarProjectname}" and "${sonarProjectname}"
                        sh """ $SCANNER_HOME/bin/sonar-scanner \
                        -Dsonar.projectName="${sonarProjectname}" \
                        -Dsonar.projectKey="${sonarProjectkey}" """           // variable (in the above line as well)
                    }
                }
                }
            }
        }

        stage('Quality Check') {
            steps {
                script {
                    waitForQualityGate abortPipeline: false, credentialsId: 'SONAR_TOKEN'   // you can choose abort pipeline to be "true"
                }
            }
        }
        
        stage("Docker Image Build") {
            steps {
                script {
                    dir("${appDirpath}") {           // variable
                            sh 'docker system prune -f'
                            sh 'docker container prune -f'
                            sh 'docker build -t ${AWS_ECR_REPO_NAME} .'
                    }
                }
            }
        }

        stage("ECR Image Pushing") {
            steps {
                script {
                        sh 'aws ecr get-login-password --region ${AWS_DEFAULT_REGION} | docker login --username AWS --password-stdin ${REPOSITORY_URI}'
                        sh 'docker tag ${AWS_ECR_REPO_NAME} ${REPOSITORY_URI}${AWS_ECR_REPO_NAME}:${BUILD_NUMBER}'
                        sh 'docker push ${REPOSITORY_URI}${AWS_ECR_REPO_NAME}:${BUILD_NUMBER}'
                }
            }
        }

        stage('Checkout Code') {
            steps {
                git url: 'https://github.com/Adithya119/Aman-Mern-CICD.git'
            }
        }

        stage('Update Deployment file') {
            environment {
                GIT_REPO_NAME = "Aman-Mern-CICD"    // value should match the github repository name
                GIT_USER_NAME = "Adithya119"
            }
            steps {
                dir("Kubernetes-Manifests-file/${k8sDirpath}") {                                 // variable                    
                    withCredentials([string(credentialsId: 'GITHUB_PAT', variable: 'GITHUB_TOKEN')]) {
                        sh '''
                            git config user.email "arkariveda@gmail.com"
                            git config user.name "Adithya"
                            BUILD_NUMBER=${BUILD_NUMBER}
                            echo $BUILD_NUMBER
                            imageTag=$(grep -oP '(?<=frontend:)[^ ]+' deployment.yaml)
                            echo $imageTag
                            sed -i "s/${AWS_ECR_REPO_NAME}:${imageTag}/${AWS_ECR_REPO_NAME}:${BUILD_NUMBER}/" deployment.yaml
                            git add deployment.yaml
                            git commit -m "Update deployment Image to version \${BUILD_NUMBER}"
                            git push https://${GITHUB_TOKEN}@github.com/${GIT_USER_NAME}/${GIT_REPO_NAME} HEAD:master
                        '''
                    }
                }
            }
        }
    }
  }
}