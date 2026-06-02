
# Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
# Click nbfs://nbhost/SystemFileSystem/Templates/Other/Dockerfile to edit this template

FROM alpine:latest

CMD ["/bin/sh"]

echo FROM openjdk:21 > Dockerfile
echo COPY target/restaurant-0.0.1-SNAPSHOT.jar app.jar >> Dockerfile
echo ENTRYPOINT ["java", "-jar", "/app.jar"] >> Dockerfile