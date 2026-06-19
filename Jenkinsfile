pipeline {
    agent {
            kubernetes {
                yaml """
    apiVersion: v1
    kind: Pod
    spec:
      containers:
      - name: docker
        image: docker:29.6.0-dind
        securityContext:
          privileged: true  # 보안 위협의 핵심 요소
        resources:
          requests:
            cpu: "1"
            memory: "2Gi"
      - name: jnlp
        image: jenkins/inbound-agent:3355.v388858a_47b_33-22
    """
            }
   }

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
                git url: 'https://github.com/chuu8319/Kafka-Kube-Clients.git', branch: 'main'
            }
        }

        stage('Build Jar') {
            steps {
                sh 'chmod +x ./gradlew'
                sh './gradlew clean build -x test'
                sh 'mkdir docker-context'
                sh 'cp build/libs/*.jar docker-context/app.jar'
                sh 'cp dockerfile docker-context/'
            }
        }

        stage('Docker Build') {
            steps {
                container('docker') {
                sh """
                    docker build -t ${REGISTRY}/${IMAGE_NAME}:${env.TAG} docker-context
                    """
                }

            }
        }

        stage('Docker Push') {
            steps {
                container('docker') {
                sh """
                    docker push ${REGISTRY}/${IMAGE_NAME}:${env.TAG}
                    """
                }
            }
        }
    }
}