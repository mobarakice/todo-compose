pipeline {
    agent any // You can specify a specific agent label if needed

    environment {
        // Define environment variables if needed
        //     ANDROID_HOME = '/home/mobarak/Android/Sdk'
        //     GRADLE_HOME = '/home/mobarak/Android/gradle_home'
        //     PATH = "$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools:$GRADLE_HOME/bin:$PATH"
            // Set environment variables, including the path to the JSON key file
            GOOGLE_PLAY_JSON_KEY = credentials('bjitApkAutoUpload')
            APK_FILE = 'app/build/outputs/apk/release/app-release.apk' // Path to your APK or AAB file
            TRACK_NAME = 'production' // Change to your desired release track (e.g., alpha, beta, production)
    }

    stages {
        stage('Checkout') {
            steps {
                // Check out your project's source code from your version control system
                // For example, if using Git:
                checkout([$class: 'GitSCM', branches: [[name: '*/main']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'bdbfdd61-6d86-4e39-a392-342e6217d2a1', url: 'https://github.com/mobarakice/todo-compose.git']]])
            }
        }

        stage('SetUpEnvironment') {
            steps {
                // Set up any environment configurations required for your build
                sh 'echo "Setting up environment..."'
                sh 'chmod +x gradlew' // Make Gradle wrapper executable if necessary
            }
        }
        stage("Build") {
            steps {
                // Run the Gradle build command
                sh './gradlew clean assembleRelease bundleRelease' // Adjust the task as needed
            }
        }
        stage('Publish Artifacts') {
            steps {
                // Archive your build artifacts for later use or distribution
                archiveArtifacts artifacts: "**/build/outputs/**/*.apk", allowEmptyArchive: true
            }
        }
        stage('Copy Artifacts') {
            steps {
                // Archive your build artifacts for later use or distribution
                copyArtifacts filter: '**/*.apk, **/*.aab', fingerprintArtifacts: true, flatten: true, includeBuildNumberInTargetPath: true, projectName: 'todocompose-pipeline-scm-script', selector: lastSuccessful(), target: '/home/mobarak/JenkinsBuild/apk/pipeline'
            }
        }

        stage("Upload aab to playstore"){
            steps {
               androidApkUpload filesPattern: '**/build/outputs/bundle/prodRelease/**/*.aab', googleCredentialsId: 'bjitApkAutoUpload', releaseName: 'Jenkins_test_release', rolloutPercentage: '100', trackName: 'internal'
            }
        }
    }
    post {
        success {
            echo 'Build successful!'
        }
        failure{
            echo 'Build failed!'
        }
    }
}