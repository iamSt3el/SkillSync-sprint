pipeline {
    agent any

    environment {
        PROJECT_ID  = 'project-00909acc-d419-4f02-8a1'
        REGION      = 'asia-south1'
        CLUSTER     = 'skillsync-cluster'
        REPO        = 'skillsync-repo'
        REGISTRY    = "${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPO}"
        TAG         = "${env.BUILD_NUMBER}"
        NAMESPACE   = 'skillsync'
    }

    tools {
        maven 'Maven-3.9'   // must match the name in Jenkins > Global Tool Configuration
    }

    stages {

        // ── 1. Checkout ────────────────────────────────────────────────────────
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        // ── 2. Build & Test all services in parallel ───────────────────────────
        stage('Build & Test') {
            parallel {
                stage('config-server')        { steps { dir('config-server')        { sh 'mvn clean package -Dmaven.test.skip=true' } } }
                stage('eureka-server')        { steps { dir('eureka-server')        { sh 'mvn clean package -Dmaven.test.skip=true' } } }
                stage('api-gateway')          { steps { dir('api-gateway')          { sh 'mvn clean package -Dmaven.test.skip=true' } } }
                stage('auth-service')         { steps { dir('auth-service')         { sh 'mvn clean package -Dmaven.test.skip=true' } } }
                stage('user-service')         { steps { dir('user-service')         { sh 'mvn clean package -Dmaven.test.skip=true' } } }
                stage('mentor-service')       { steps { dir('mentor-service')       { sh 'mvn clean package -Dmaven.test.skip=true' } } }
                stage('skill-service')        { steps { dir('skill-service')        { sh 'mvn clean package -Dmaven.test.skip=true' } } }
                stage('session-service')      { steps { dir('session-service')      { sh 'mvn clean package -Dmaven.test.skip=true' } } }
                stage('group-service')        { steps { dir('group-service')        { sh 'mvn clean package -Dmaven.test.skip=true' } } }
                stage('review-service')       { steps { dir('review-service')       { sh 'mvn clean package -Dmaven.test.skip=true' } } }
                stage('notification-service') { steps { dir('notification-service') { sh 'mvn clean package -Dmaven.test.skip=true' } } }
                stage('payment-service')      { steps { dir('payment-service')      { sh 'mvn clean package -Dmaven.test.skip=true' } } }
            }
        }

        // ── 3. Authenticate with GCP ───────────────────────────────────────────
        stage('GCP Auth') {
            steps {
                sh '''
                    gcloud config set project $PROJECT_ID
                    gcloud auth configure-docker ${REGION}-docker.pkg.dev --quiet
                '''
            }
        }

        // ── 4. Docker build & push all images in parallel ─────────────────────
        stage('Docker Build & Push') {
            parallel {
                stage('config-server')        { steps { sh "docker build -t ${REGISTRY}/config-server:${TAG} ./config-server && docker push ${REGISTRY}/config-server:${TAG}" } }
                stage('eureka-server')        { steps { sh "docker build -t ${REGISTRY}/eureka-server:${TAG} ./eureka-server && docker push ${REGISTRY}/eureka-server:${TAG}" } }
                stage('api-gateway')          { steps { sh "docker build -t ${REGISTRY}/api-gateway:${TAG} ./api-gateway && docker push ${REGISTRY}/api-gateway:${TAG}" } }
                stage('auth-service')         { steps { sh "docker build -t ${REGISTRY}/auth-service:${TAG} ./auth-service && docker push ${REGISTRY}/auth-service:${TAG}" } }
                stage('user-service')         { steps { sh "docker build -t ${REGISTRY}/user-service:${TAG} ./user-service && docker push ${REGISTRY}/user-service:${TAG}" } }
                stage('mentor-service')       { steps { sh "docker build -t ${REGISTRY}/mentor-service:${TAG} ./mentor-service && docker push ${REGISTRY}/mentor-service:${TAG}" } }
                stage('skill-service')        { steps { sh "docker build -t ${REGISTRY}/skill-service:${TAG} ./skill-service && docker push ${REGISTRY}/skill-service:${TAG}" } }
                stage('session-service')      { steps { sh "docker build -t ${REGISTRY}/session-service:${TAG} ./session-service && docker push ${REGISTRY}/session-service:${TAG}" } }
                stage('group-service')        { steps { sh "docker build -t ${REGISTRY}/group-service:${TAG} ./group-service && docker push ${REGISTRY}/group-service:${TAG}" } }
                stage('review-service')       { steps { sh "docker build -t ${REGISTRY}/review-service:${TAG} ./review-service && docker push ${REGISTRY}/review-service:${TAG}" } }
                stage('notification-service') { steps { sh "docker build -t ${REGISTRY}/notification-service:${TAG} ./notification-service && docker push ${REGISTRY}/notification-service:${TAG}" } }
                stage('payment-service')      { steps { sh "docker build -t ${REGISTRY}/payment-service:${TAG} ./payment-service && docker push ${REGISTRY}/payment-service:${TAG}" } }
            }
        }

        // ── 5. Connect kubectl to GKE ──────────────────────────────────────────
        stage('Connect to GKE') {
            steps {
                sh '''
                    gcloud container clusters get-credentials $CLUSTER --region $REGION --project $PROJECT_ID
                '''
            }
        }

        // ── 6. Apply namespace, secrets, configmap ─────────────────────────────
        stage('Apply Base Configs') {
            steps {
                sh '''
                    kubectl apply -f k8s/namespace.yaml
                    kubectl apply -f k8s/secrets.yaml
                    kubectl apply -f k8s/configmap.yaml
                '''
            }
        }

        // ── 7. Deploy infrastructure (MySQL, RabbitMQ, Zipkin) ─────────────────
        stage('Deploy Infrastructure') {
            steps {
                sh '''
                    kubectl apply -f k8s/infrastructure/mysql.yaml
                    kubectl apply -f k8s/infrastructure/rabbitmq.yaml
                    kubectl apply -f k8s/infrastructure/zipkin.yaml

                    # Wait for MySQL and RabbitMQ to be ready before continuing
                    kubectl rollout status statefulset/mysql    -n $NAMESPACE --timeout=180s
                    kubectl rollout status statefulset/rabbitmq -n $NAMESPACE --timeout=180s
                '''
            }
        }

        // ── 8. Deploy config-server first, wait until ready ────────────────────
        stage('Deploy config-server') {
            steps {
                sh """
                    kubectl set image deployment/config-server config-server=${REGISTRY}/config-server:${TAG} -n ${NAMESPACE}
                    kubectl apply -f k8s/services/config-server.yaml
                    kubectl rollout status deployment/config-server -n ${NAMESPACE} --timeout=120s
                """
            }
        }

        // ── 9. Deploy eureka-server second, wait until ready ───────────────────
        stage('Deploy eureka-server') {
            steps {
                sh """
                    kubectl apply -f k8s/services/eureka-server.yaml
                    kubectl set image deployment/eureka-server eureka-server=${REGISTRY}/eureka-server:${TAG} -n ${NAMESPACE}
                    kubectl rollout status deployment/eureka-server -n ${NAMESPACE} --timeout=120s
                """
            }
        }

        // ── 10. Deploy all business services in parallel ───────────────────────
        stage('Deploy Business Services') {
            parallel {
                stage('auth-service') {
                    steps {
                        sh """
                            kubectl apply -f k8s/services/auth-service.yaml
                            kubectl set image deployment/auth-service auth-service=${REGISTRY}/auth-service:${TAG} -n ${NAMESPACE}
                        """
                    }
                }
                stage('user-service') {
                    steps {
                        sh """
                            kubectl apply -f k8s/services/user-service.yaml
                            kubectl set image deployment/user-service user-service=${REGISTRY}/user-service:${TAG} -n ${NAMESPACE}
                        """
                    }
                }
                stage('mentor-service') {
                    steps {
                        sh """
                            kubectl apply -f k8s/services/mentor-service.yaml
                            kubectl set image deployment/mentor-service mentor-service=${REGISTRY}/mentor-service:${TAG} -n ${NAMESPACE}
                        """
                    }
                }
                stage('skill-service') {
                    steps {
                        sh """
                            kubectl apply -f k8s/services/skill-service.yaml
                            kubectl set image deployment/skill-service skill-service=${REGISTRY}/skill-service:${TAG} -n ${NAMESPACE}
                        """
                    }
                }
                stage('session-service') {
                    steps {
                        sh """
                            kubectl apply -f k8s/services/session-service.yaml
                            kubectl set image deployment/session-service session-service=${REGISTRY}/session-service:${TAG} -n ${NAMESPACE}
                        """
                    }
                }
                stage('group-service') {
                    steps {
                        sh """
                            kubectl apply -f k8s/services/group-service.yaml
                            kubectl set image deployment/group-service group-service=${REGISTRY}/group-service:${TAG} -n ${NAMESPACE}
                        """
                    }
                }
                stage('review-service') {
                    steps {
                        sh """
                            kubectl apply -f k8s/services/review-service.yaml
                            kubectl set image deployment/review-service review-service=${REGISTRY}/review-service:${TAG} -n ${NAMESPACE}
                        """
                    }
                }
                stage('notification-service') {
                    steps {
                        sh """
                            kubectl apply -f k8s/services/notification-service.yaml
                            kubectl set image deployment/notification-service notification-service=${REGISTRY}/notification-service:${TAG} -n ${NAMESPACE}
                        """
                    }
                }
                stage('payment-service') {
                    steps {
                        sh """
                            kubectl apply -f k8s/services/payment-service.yaml
                            kubectl set image deployment/payment-service payment-service=${REGISTRY}/payment-service:${TAG} -n ${NAMESPACE}
                        """
                    }
                }
            }
        }

        // ── 11. Deploy api-gateway last ────────────────────────────────────────
        stage('Deploy api-gateway') {
            steps {
                sh """
                    kubectl apply -f k8s/services/api-gateway.yaml
                    kubectl set image deployment/api-gateway api-gateway=${REGISTRY}/api-gateway:${TAG} -n ${NAMESPACE}
                    kubectl rollout status deployment/api-gateway -n ${NAMESPACE} --timeout=120s
                """
            }
        }

    }

    post {
        success {
            echo "=== Deployment successful! Build #${env.BUILD_NUMBER} ==="
            sh "kubectl get service api-gateway -n ${NAMESPACE}"   // prints the external IP
        }
        failure {
            echo "=== Pipeline failed at stage. Check logs above. ==="
        }
    }
}
