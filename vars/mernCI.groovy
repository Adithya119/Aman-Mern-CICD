def call() {
    sh 'docker system prune -f'
    sh 'docker container prune -f'
    sh 'docker build -t ${AWS_ECR_REPO_NAME} .'
}