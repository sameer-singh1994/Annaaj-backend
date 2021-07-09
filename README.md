# Annaaj Backend
 Annaaj backend apis


* This is a Maven Project. Ensure, Maven is installed on your system.
* It is Recommended that you use Linux Based OS.
* It might happen that you have installed XAMPP/LAMPP software (by Bitnami) on your system. Instead of using the db provided by XAMPP/LAMPP by bitnami, it is recommended that you install ``mariadb-server`` and use it as  database while running this application.  

###How to run in local
1. Change the Application Properties (E.g. username/password of DB) present in ``resources/application.properties``  according to your local mysql-server.
1. Go to application.properties and comment / uncomment the corresponding front-end url and enter the STRIPE API Keys
1. Create a database called `ecommerce` with ``CHARACTER SET utf8mb4`` and `COLLATE utf8mb4_0900_ai_ci`. MariaDB does not support `COLLATE utf8mb4_0900_ai_ci`. So, if you are using MariaDB, open `database-dump.sql` file and replace `COLLATE utf8mb4_0900_ai_ci` with `COLLATE utf8mb4_general_ci`  
1. To run the application, run the command ``sh run.sh`` i.e. execute the ``run.sh`` file. 
1. After starting application, go to http://localhost:8443/api/swagger-ui.html#/


### In case of "java.lang.IllegalStateException: Unable to load cache item error":
- Go into pom.xml and comment out the scope for the "spring-boot-starter-tomcat" dependency


#updating application.properties
update the ``file.storage.location`` to local path where you want to store the images

update ``admin.username`` and ``admin.password`` which will be used to access the APIs usable by ADMIN

