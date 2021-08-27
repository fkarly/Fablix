# Cool Project

## Project 1 Videos

Trial 1:
	[Part 1](https://youtu.be/YR-o0lngqlA), [Part 2](https://youtu.be/KOC4AnX3HXU)

Trial 2:
	[Part 1](https://youtu.be/CSqydo5LKSU), [Part 2](https://youtu.be/m7N8T_4Pjvs)
Trial 3:
	[Trial 3](https://youtu.be/IQ7fB095i2I)

## Project 2 Videos

https://youtu.be/CZ9CwhQsCkc

Instruction of deployment:
Every page has its own Servlet, which makes it very easy to debug since every servlet is independent from one to another.
User has to login in order to browse and purchase movie(s) from Fablix.
User also needs to provide the correct credentials in order to make a successfull payment.
User can choose to sort by 10,25,50,100. User can't also click the previous button if they're on page one.
Page number and sorting are maintain whenever the user clicks back to search.
Substring macthing design:
%String%, meaning there could be any char/string in front and back. This applies to title, director, and star name.
For year, it has to be the exact, else won't load.


mysql:
	username: mytestuser
	password: My6$Password

## Project 3 Videos

https://youtu.be/pDl6k-NUJnU

Changed query statement to prepared statement in order to avoid scam.

## Project 4 Videos

https://youtu.be/dloCMVtp49w

*Can't run the demo version for android; thus, unable to complete.

## Project 5

- # General
    - #### Team#: 107
    
    - #### Names: Fentiano Karly
    
    - #### Project 5 Video Demo Link: None

    - #### Instruction of deployment: For each instances, mvn package. Make sure that the Api_balancer contains the correct private ip address of both master and slaves. 

    - #### Collaborations and Work Distribution: Individual


- # Connection Pooling
    - #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling. 
	All servlets that uses Data Source, WebContent/META-INF/context.html.
    
    - #### Explain how Connection Pooling is utilized in the Fabflix code.
	We set the maximum amount of connection to a certain amount and set the bar for maximum amount of idle connection before it gets freed. This will allows us to use the resources more efficiently. This will also increase the website performance since it doesn't have to establish a new connection everytime we want to talk to the database server.
    
    - #### Explain how Connection Pooling works with two backend SQL.
    	We cache the prepared statement so that we could use the connection pool instead of having each prepared statement with one connection.

- # Master/Slave
    - #### Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.
	mysqldMasters.cnf for masters, mysqldSlave.cnf for slave.	

    - #### How read/write requests were routed to Master/Slave SQL?
	Using redirection to redirect the write queries to master. For read, it could use any of the instances.
	