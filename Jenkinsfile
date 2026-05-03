pipeline {
    agent any

    environment {
        AWS_ACCOUNT_ID = '029422951382'
        REGION         = 'eu-north-1'
        CLUSTER        = 'skillsync-cluster'
        REGISTRY       = "${AWS_ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com/skillsync-repo"
        TAG            = "${env.BUILD_NUMBER}"
        NAMESPACE      = 'skillsync'
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
                stage('auth-service')         { steps { dir('auth-service')         { sh 'mvn clean verify' } } }
                stage('user-service')         { steps { dir('user-service')         { sh 'mvn clean verify' } } }
                stage('mentor-service')       { steps { dir('mentor-service')       { sh 'mvn clean verify' } } }
                stage('skill-service')        { steps { dir('skill-service')        { sh 'mvn clean verify' } } }
                stage('session-service')      { steps { dir('session-service')      { sh 'mvn clean verify' } } }
                stage('group-service')        { steps { dir('group-service')        { sh 'mvn clean verify' } } }
                stage('review-service')       { steps { dir('review-service')       { sh 'mvn clean verify' } } }
                stage('notification-service') { steps { dir('notification-service') { sh 'mvn clean verify' } } }
                stage('payment-service')      { steps { dir('payment-service')      { sh 'mvn clean package -Dmaven.test.skip=true' } } }
            }
        }

        // ── 2b. Publish test results ───────────────────────────────────────────
        stage('Publish Test Results') {
            steps {
                junit(
                    testResults: '**/target/surefire-reports/*.xml',
                    allowEmptyResults: true
                )
            }
        }

        // ── 3. Authenticate with AWS ECR ──────────────────────────────────────
        stage('AWS ECR Auth') {
            steps {
                withCredentials([[
                    $class: 'AmazonWebServicesCredentialsBinding',
                    credentialsId: 'aws-credentials'
                ]]) {
                    sh 'aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $REGISTRY'
                }
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

        // ── 5. Connect kubectl to EKS ─────────────────────────────────────────
        stage('Connect to EKS') {
            steps {
                withCredentials([[
                    $class: 'AmazonWebServicesCredentialsBinding',
                    credentialsId: 'aws-credentials'
                ]]) {
                    sh 'aws eks update-kubeconfig --region $REGION --name $CLUSTER'
                }
            }
        }

        // ── 6. Apply namespace, secrets, configmap ─────────────────────────────
        stage('Apply Base Configs') {
            steps {
                withCredentials([[
                    $class: 'AmazonWebServicesCredentialsBinding',
                    credentialsId: 'aws-credentials'
                ]]) {
                    sh '''
                        kubectl apply -f k8s/namespace.yaml
                        kubectl apply -f k8s/secrets.yaml
                        kubectl apply -f k8s/configmap.yaml
                    '''
                }
            }
        }

        // ── 7. Deploy infrastructure (MySQL, RabbitMQ, Zipkin, SonarQube …) ───────
        stage('Deploy Infrastructure') {
            steps {
                withCredentials([[
                    $class: 'AmazonWebServicesCredentialsBinding',
                    credentialsId: 'aws-credentials'
                ]]) {
                    sh '''
                        kubectl apply -f k8s/infrastructure/mysql.yaml
                        kubectl apply -f k8s/infrastructure/rabbitmq.yaml
                        kubectl apply -f k8s/infrastructure/redis.yaml
                        kubectl apply -f k8s/infrastructure/zipkin.yaml
                        kubectl apply -f k8s/infrastructure/prometheus.yaml
                        kubectl apply -f k8s/infrastructure/grafana.yaml
                        kubectl apply -f k8s/infrastructure/postgres-sonarqube.yaml
                        kubectl apply -f k8s/infrastructure/sonarqube.yaml

                        kubectl rollout status statefulset/mysql    -n $NAMESPACE --timeout=600s
                        kubectl rollout status statefulset/rabbitmq -n $NAMESPACE --timeout=600s
                        kubectl rollout status deployment/redis     -n $NAMESPACE --timeout=180s
                    '''
                }
            }
        }

        // ── 8. Deploy config-server first, wait until ready ────────────────────
        stage('Deploy config-server') {
            steps {
                withCredentials([[
                    $class: 'AmazonWebServicesCredentialsBinding',
                    credentialsId: 'aws-credentials'
                ]]) {
                    sh """
                        sed 's|AWS_ACCOUNT_ID_PLACEHOLDER|${AWS_ACCOUNT_ID}|g; s|REGION_PLACEHOLDER|${REGION}|g; s|:latest|:${TAG}|g' k8s/services/config-server.yaml | kubectl apply -f -
                        kubectl rollout status deployment/config-server -n ${NAMESPACE} --timeout=300s
                    """
                }
            }
        }

        // ── 9. Deploy eureka-server second, wait until ready ───────────────────
        stage('Deploy eureka-server') {
            steps {
                withCredentials([[
                    $class: 'AmazonWebServicesCredentialsBinding',
                    credentialsId: 'aws-credentials'
                ]]) {
                    sh """
                        sed 's|AWS_ACCOUNT_ID_PLACEHOLDER|${AWS_ACCOUNT_ID}|g; s|REGION_PLACEHOLDER|${REGION}|g; s|:latest|:${TAG}|g' k8s/services/eureka-server.yaml | kubectl apply -f -
                        kubectl rollout status deployment/eureka-server -n ${NAMESPACE} --timeout=300s
                    """
                }
            }
        }

        // ── 10. Deploy all business services in parallel ───────────────────────
        stage('Deploy Business Services') {
            parallel {
                stage('auth-service')         { steps { withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials']]) { sh "sed 's|AWS_ACCOUNT_ID_PLACEHOLDER|${AWS_ACCOUNT_ID}|g; s|REGION_PLACEHOLDER|${REGION}|g; s|:latest|:${TAG}|g' k8s/services/auth-service.yaml | kubectl apply -f -" } } }
                stage('user-service')         { steps { withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials']]) { sh "sed 's|AWS_ACCOUNT_ID_PLACEHOLDER|${AWS_ACCOUNT_ID}|g; s|REGION_PLACEHOLDER|${REGION}|g; s|:latest|:${TAG}|g' k8s/services/user-service.yaml | kubectl apply -f -" } } }
                stage('mentor-service')       { steps { withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials']]) { sh "sed 's|AWS_ACCOUNT_ID_PLACEHOLDER|${AWS_ACCOUNT_ID}|g; s|REGION_PLACEHOLDER|${REGION}|g; s|:latest|:${TAG}|g' k8s/services/mentor-service.yaml | kubectl apply -f -" } } }
                stage('skill-service')        { steps { withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials']]) { sh "sed 's|AWS_ACCOUNT_ID_PLACEHOLDER|${AWS_ACCOUNT_ID}|g; s|REGION_PLACEHOLDER|${REGION}|g; s|:latest|:${TAG}|g' k8s/services/skill-service.yaml | kubectl apply -f -" } } }
                stage('session-service')      { steps { withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials']]) { sh "sed 's|AWS_ACCOUNT_ID_PLACEHOLDER|${AWS_ACCOUNT_ID}|g; s|REGION_PLACEHOLDER|${REGION}|g; s|:latest|:${TAG}|g' k8s/services/session-service.yaml | kubectl apply -f -" } } }
                stage('group-service')        { steps { withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials']]) { sh "sed 's|AWS_ACCOUNT_ID_PLACEHOLDER|${AWS_ACCOUNT_ID}|g; s|REGION_PLACEHOLDER|${REGION}|g; s|:latest|:${TAG}|g' k8s/services/group-service.yaml | kubectl apply -f -" } } }
                stage('review-service')       { steps { withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials']]) { sh "sed 's|AWS_ACCOUNT_ID_PLACEHOLDER|${AWS_ACCOUNT_ID}|g; s|REGION_PLACEHOLDER|${REGION}|g; s|:latest|:${TAG}|g' k8s/services/review-service.yaml | kubectl apply -f -" } } }
                stage('notification-service') { steps { withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials']]) { sh "sed 's|AWS_ACCOUNT_ID_PLACEHOLDER|${AWS_ACCOUNT_ID}|g; s|REGION_PLACEHOLDER|${REGION}|g; s|:latest|:${TAG}|g' k8s/services/notification-service.yaml | kubectl apply -f -" } } }
                stage('payment-service')      { steps { withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials']]) { sh "sed 's|AWS_ACCOUNT_ID_PLACEHOLDER|${AWS_ACCOUNT_ID}|g; s|REGION_PLACEHOLDER|${REGION}|g; s|:latest|:${TAG}|g' k8s/services/payment-service.yaml | kubectl apply -f -" } } }
            }
        }

        // ── 11. Deploy api-gateway last ────────────────────────────────────────
        stage('Deploy api-gateway') {
            steps {
                withCredentials([[
                    $class: 'AmazonWebServicesCredentialsBinding',
                    credentialsId: 'aws-credentials'
                ]]) {
                    sh """
                        sed 's|AWS_ACCOUNT_ID_PLACEHOLDER|${AWS_ACCOUNT_ID}|g; s|REGION_PLACEHOLDER|${REGION}|g; s|:latest|:${TAG}|g' k8s/services/api-gateway.yaml | kubectl apply -f -
                        kubectl rollout status deployment/api-gateway -n ${NAMESPACE} --timeout=300s
                    """
                }
            }
        }

        // ── 12. Apply Ingress ──────────────────────────────────────────────────
        stage('Apply Ingress') {
            steps {
                withCredentials([[
                    $class: 'AmazonWebServicesCredentialsBinding',
                    credentialsId: 'aws-credentials'
                ]]) {
                    sh '''
                        kubectl apply -f k8s/ingress.yaml
                        kubectl apply -f k8s/ingress-redirects.yaml
                        kubectl apply -f k8s/ingress-tools.yaml
                        kubectl apply -f k8s/ingress-eureka-static.yaml
                        kubectl apply -f k8s/ingress-zipkin-api.yaml
                        echo "Waiting for ingress IP..."
                        kubectl get ingress skillsync-ingress -n skillsync
                    '''
                }
            }
        }

        // ── 13. SonarQube Analysis ─────────────────────────────────────────────
        // Prerequisites (one-time setup):
        //   1. Wait for SonarQube to finish starting: http://<IP>/sonarqube
        //   2. Login as admin / admin, change the password when prompted
        //   3. My Account → Security → Generate Token (type: Global Analysis Token)
        //   4. Jenkins → Manage Jenkins → Credentials → Add:
        //        Kind: Secret text  |  ID: sonar-token  |  Secret: <token>
        //
        // catchError keeps the overall build GREEN if SonarQube isn't ready yet
        // (e.g. on the very first run before it has finished starting up).
        stage('SonarQube Analysis') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                        script {
                            def sonarUrl = "https://skillsync.mooo.com/sonarqube"
                            def services = [
                                'config-server', 'eureka-server', 'api-gateway',
                                'auth-service', 'user-service', 'mentor-service',
                                'skill-service', 'session-service', 'group-service',
                                'review-service', 'notification-service', 'payment-service'
                            ]
                            def jobs = [:]
                            services.each { svc ->
                                def s = svc   // capture variable for parallel closure
                                jobs[s] = {
                                    dir(s) {
                                        sh """
                                            mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                                                -Dsonar.host.url=${sonarUrl} \
                                                -Dsonar.token=${SONAR_TOKEN} \
                                                -Dsonar.projectKey=${s} \
                                                -Dsonar.projectName=${s} \
                                                -Dmaven.test.skip=true
                                        """
                                    }
                                }
                            }
                            parallel jobs
                        }
                    }
                }
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
