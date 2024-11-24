def call(String blockName) {                     // By defining def call(), you allow the file to be invoked directly as a function
    
    switch (blockname) {                         // Select a block of code based on the provided block name
      
      case 'CleaningWorkspaceAndCheckoutFromGit':       // If the block name is 'CleaningWorkspaceAndCheckoutFromGit'
        
        return {                                             // Return a closure (a reusable block of code)
          
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

        }

      
      case 'QualityCheck':
        
        return {

          stage('Quality Check') {
              steps {
                  script {
                      waitForQualityGate abortPipeline: false, credentialsId: 'SONAR_TOKEN'   // you can choose abortPipeline to be "true"
                  }
              }
          }

        }


      case 'EcrImagePushAndCheckoutCode':

        return {
        
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

        }
    }
}