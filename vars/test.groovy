def call(Map config = [:]) {
    pipeline {
        agent any
        stages {
            stage('Build') {
                steps {
                    echo "Building project..."
                    sh "echo Build logic here"
                }
            }
            stage('Test') {
                steps {
                    echo "Running tests..."
                    sh "echo Test logic here"
                }
            }
        }
    }
}