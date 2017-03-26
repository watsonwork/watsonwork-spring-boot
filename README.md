[![Build Status](https://travis-ci.org/watsonwork/watsonwork-spring-boot.svg?branch=master)](https://travis-ci.org/watsonwork/watsonwork-spring-boot)

**Got a question?** Message me on [Watson Workspace](https://watsonwork.me/bhumitpatel@ie.ibm.com)

# watsonwork-spring-boot

A Watson Work Services app written in Spring Boot. This project allows an application to connect to Watson Work Services 
to receive and send messages via Webhook API.
 
The main purpose of this project is to provide third party app developers to be able to hook in their app logic without
worrying about writing your own implementation from scratch. 

# API Supported by the app
- create message in a space
- set app photo
    - By default the app looks for a file named `app-photo.jpg` in `src/main/reources` directory. The photo will be uploaded
    one the app start by using the [@PostConstruct](http://docs.oracle.com/javaee/7/api/javax/annotation/PostConstruct.html) annotation
- upload file to a space
- authorize the application to act on behalf of a user


## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. 
See deployment for notes on how to deploy the project on a live system.

## Creating Watson Work Services App

1. In your Web browser, go to [Watson Work Services / Apps](https://developer.watsonwork.ibm.com/apps)
2. Click on `Create new app`
3. Give your app an unique name with description and click on `Create`
4. Take a note of the `App ID` and `App Secret`
5. Click on `Listen to Events -> Add an outbound webhook`
6. In the callback URL, specify the URL for your app. This code assumes that the webhook listener is at https://yoururl/webhook so don't forget to add /webhook to the end of the URL (_if you don't know where the app will be deployed, use a sample URL for now, like https://test.acme.com/webhook and you can modify that later_)
7. Take a note of Webhook Secret

**NOTE:** _**Do not commit your app Id, secret, and webhook secret when pushing your changes to Github**_

### Running locally using IntelliJ IDEA

Prerequisite for running the app using IntelliJ IDEA:
- Install [Lombok Plugin](https://plugins.jetbrains.com/plugin/6317-lombok-plugin) for IntelliJ IDEA
- Install [Ngrok](https://ngrok.com/) - used for testing the app locally without deploying on any PaaS

1. [Fork it](https://github.com/watsonwork/watsonwork-spring-boot/fork)
2. Clone the project `git clone https://github.com/YOUR_GITHUB_USERNAME/watsonwork-spring-boot.git`
3. Open IntelliJ IDEA `File -> New -> Project from Exisiting Sources... `
4. Navigate to the project and select `build.gradle`. Click `OK`
5. Click `OK` on Import Project from Gradle window
6. Open `src/main/resources/application.yml`

```yaml
    watsonwork:
            webhook:
                secret: ${WEBHOOK_SECRET:of7bs9evq4lnbi9slg0qq2k7z6nsfk7y} #replace of7bs9evq4lnbi9slg0qq2k7z6nsfk7y with your webhook secret 
            app:
                id: ${APP_ID:c79e3474-e963-4024-aa47-4a1087903381} #replace c79e3474-e963-4024-aa47-4a1087903381 with your app Id 
                secret: ${APP_SECRET:javltqgjfvjh2d99zj5bjdfr0q4x5lw3} #replace javltqgjfvjh2d99zj5bjdfr0q4x5lw3 with your app secret 
            api:
                uri: ${WATSON_WORK_API_URI:https://api.watsonwork.ibm.com} 
 ```
7. Right click on `ApplicationBoostrap.java` `->` `Run ApplicationBootstrap..`
8. Open up terminal where ngrok is installed and run the command `ngrok.exe http 9080` on windows or `./ngrok http 9080` on unix. By default the app runs on `http port 9080`. This exposes your app via a publicly accessible URL. ngrok displays a forwarding url after executing the command above which might look as such: `http://sd2323.ngrok.io`. Take a note of that URL
9. Navigate to your app on [Watson Work Services / Apps](https://developer.watsonwork.ibm.com/apps). Select `Listen to Events`. Edit the webhook configuration with the new publicly available URL from ngrok. Make sure your callback URL has /webhook path. For example: `http://sd2323.ngrok.io/webhook`
10. Select `message-created` Event. Click `Save` and `Enable` the webhook
11. Add your app to a space on Watson Workspace and watch it echo your messages..

## Deployment

### Deploying  on IBM Bluemix

Assuming you have completed steps 1-6 in `Running locally using IntelliJ IDEA`

1. Sign up for a free trial on [Bluemix](https://console.ng.bluemix.net/)
2. Install the [Cloud Foundry CLI](https://github.com/cloudfoundry/cli/releases) and then Install the [Bluemix CLI](http://clis.ng.bluemix.net/ui/home.html). On Windows, be sure to add the bluemix cli to your PATH.
3. Run `bluemix login -a https://api.ng.bluemix.net`, and enter your email and password when prompted
4. Open terminal in the project root. Run `./gradlew clean build` on linux/mac or `gradlew clean build` on windows.
5. Run `cf push my-app-name -p build/watsonwork-spring-boot-X.X.X.jar -m 512m` (Tip: Make sure the name you want to use is not taken on Bluemix already, since it must be unique.)
6. When it's finished pushing to bluemix, visit your app's url.
7. Navigate to your app on [Watson Work Services / Apps](https://developer.watsonwork.ibm.com/apps). Select `Listen to Events`. Edit the webhook configuration with the new publicly available URL from Bluemix. Make sure your callback URL has /webhook path. For example: `https://my-host-name.mybluemix.net/webhook`


## Built With

* [Spring Boot 1.5.2](https://projects.spring.io/spring-boot) - Web Framework
* [Gradle](https://gradle.org/) - Dependency Management
