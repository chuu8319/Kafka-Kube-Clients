pipeline {
    agent any

    environment {
        REGISTRY = "192.168.10.20:5000"
        IMAGE_NAME = "kube-producer"
        TAG = ""
    }

    stages {
        stage('init') {
            steps {
                script {
                    env.TAG = new Date().format("yyyyMMdd")
                }
            }
        }

        stage('Checkout') {
            steps {
                git url: 'https://your-repo-url.git', branch: 'main'
            }
        }

        stage('Build Jar') {
            steps {
                sh './gradlew clean build -x test'
            }
        }

        stage('Docker Build') {
            steps {
                sh """
                docker build -t ${REGISTRY}/${IMAGE_NAME}:${TAG} .
                """
            }
        }

        stage('Docker Push') {
            steps {
                sh """
                docker push ${REGISTRY}/${IMAGE_NAME}:${TAG}
                """
            }
        }
    }
}