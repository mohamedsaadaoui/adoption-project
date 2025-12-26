pipeline {
    agent any

    tools {
        maven 'M3'
        jdk 'JDK17'
    }

    environment {
        DOCKERHUB_CREDENTIALS = credentials('docker-hub-credentials')
        SONAR_SCANNER_HOME = tool 'SonarScanner'
    }

    stages {

        stage('Checkout Git') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/mohamedsaadaoui/adoption-project.git'
            }
        }

        // 🟢 1️⃣ STAGE OBLIGATOIRE : TESTS UNITAIRES
        stage('Tests unitaires JUnit') {
            steps {
                bat 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        // 🟢 2️⃣ COMPILATION APRÈS TESTS
        stage('Compile & Package') {
            steps {
                bat 'mvn clean package -DskipTests'
            }
            post {
                always {
                    archiveArtifacts 'target/adoption-Project-0.0.1-SNAPSHOT.jar'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonar-server') {
                    bat 'mvn sonar:sonar -Dsonar.projectKey=adoption-project -Dsonar.projectName=Adoption-Project'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("mohamedsaadaouii/adoption-app:${env.BUILD_NUMBER}")
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                script {
                    docker.withRegistry('', 'docker-hub-credentials') {
                        docker.image("mohamedsaadaouii/adoption-app:${env.BUILD_NUMBER}").push()
                        docker.image("mohamedsaadaouii/adoption-app:${env.BUILD_NUMBER}").push("latest")
                    }
                }
            }
        }

        stage('Deploy with Docker Compose') {
            steps {
                bat """
                    echo ===== Stop and Remove Old Containers =====
                    docker rm -f adoption-mysql adoption-prometheus adoption-cadvisor adoption-grafana adoption-spring-app || echo "No existing containers to remove"

                    echo ===== Pull Latest Images =====
                    docker-compose pull

                    echo ===== Start Containers =====
                    docker-compose up -d
                """
            }
        }

        stage('API Tests') {
            steps {
                bat 'curl -X GET http://localhost:8089/adoption/actuator/health'
            }
        }
    }

    post {
        always { cleanWs() }
        success { echo "✅ Pipeline Build & Deploy réussi !" }
        failure { echo "❌ Pipeline échoué, vérifier les logs." }
    }
}
