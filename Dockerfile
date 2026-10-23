# 1. Usar la imagen base oficial de liberica con java 25
FROM bellsoft/liberica-openjdk-alpine:25

# 2. Crear un directorio de trabajo para la aplicación
WORKDIR /app

# 3. Copiar el archivo JAR empaquetado de tu aplicación (ajusta la ruta según tu proyecto, ej. Maven o Gradle)
COPY target/pedido-service.jar app.jar

# 4. Exponer el puerto en el que corre tu aplicación (por ejemplo, 8080)
EXPOSE 8083

#Enviando datos para que funcione adentro del docker
ENV EUREKA_URL=http://eureka-service:8761/eureka/
ENV DB_HOST=mysql-grupo7
ENV DB_PASSWORD=admindocker

# 5. Comando para ejecutar la aplicación al iniciar el contenedor
ENTRYPOINT ["java", "-jar", "app.jar"]

