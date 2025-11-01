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

        stage('Compile & Tests') {
            steps {
                sh 'mvn clean compile test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    archiveArtifacts 'target/*.jar'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonar-server') {
                    sh 'mvn sonar:sonar -Dsonar.projectKey=adoption-project -Dsonar.projectName=Adoption-Project'
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
                sh '''
                    docker-compose down
                    docker-compose pull
                    docker-compose up -d
                '''
            }
        }

        stage('API Tests') {
            steps {
                sh '''
                    curl -X GET http://localhost:8089/adoption/actuator/health
                '''
            }
        }
    }

    post {
        always { cleanWs() }
        success { echo "✅ Pipeline Build & Deploy réussi !" }
        failure { echo "❌ Pipeline échoué, vérifier les logs." }
    }
}
