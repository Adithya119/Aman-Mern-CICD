def call(Map config = [:]) {
    pipeline {
        agent any
        stages {
            stage('Build') {
                steps {
                    echo "Building project: ${config.projectName}"
                    sh "echo Build logic here"
                }
            }
            stage('Test') {
                steps {
                    echo "Running tests for ${config.projectName}"
                    sh "echo Test logic here"
                }
            }
        }
    }
}