FROM openjdk:17
WORKDIR /home/ec2-user/
COPY tricount-clone-0.0.1-SNAPSHOT.jar .
CMD java -jar tricount-clone-0.0.1-SNAPSHOT.jar