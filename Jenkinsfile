pipeline {
    agent any

    environment {
        APP_NAME = 'excuse-generator'
        DOCKER_NETWORK = 'devops-net'
        HOST_PORT = '8080'
        CONTAINER_PORT = '8080'
    }

    stages {
        stage('Verify & Test with Maven') {
            steps {
                sh '''
                    docker run --rm \
                      -v jenkins_home:/var/jenkins_home \
                      -w "$WORKSPACE" \
                      maven:3.9-eclipse-temurin-21 \
                      sh -c "mvn -v && mvn -B clean verify"
                '''
            }
            post {
                always {
                    junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t ${APP_NAME}:${BUILD_NUMBER} -t ${APP_NAME}:latest .'
            }
        }

        stage('Deploy Container') {
            steps {
                sh '''
                    docker stop ${APP_NAME} || true
                    docker rm ${APP_NAME} || true
                    docker run -d \
                      --name ${APP_NAME} \
                      --restart unless-stopped \
                      --network ${DOCKER_NETWORK} \
                      -p ${HOST_PORT}:${CONTAINER_PORT} \
                      ${APP_NAME}:latest
                    docker image prune -f
                '''
            }
        }
    }
}

