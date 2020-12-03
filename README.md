# Food Factory engine

## Configuration
Configuration file is in src/main/resources/config.properties
Configuration parameters are: Number of Assembly lines, Number of ovens with their sizes, Number of Stores with their sizes

## Build requirements
Java 1.8, Maven 3.3.9

## Build
mvn clean install

## Program running
java -jar target/foodfactory-0.0.1-SNAPSHOT.jar

Number of products, their sizes and cooking times are generated automatically after start for every assembly line.
Every assembly line spawns new thread (AsseblyLineTask), which is trying to put the product in one of the ovens, where enough place for the product is available.
If free space in the oven is found, new cooking thread (CookingTask) is spawned, which cooks the product for the right time and then it puts the product back on the origin assembly line.
If there is no space in the ovens, assembly line is trying to find place in one of the stores. If there is enough space in one of the stores, new store thread (StoreTask) is spavned, which is trying to put the products in the free oven.
In no free space is found in the ovens, it sleeps for 0,5s and tries to find free space again. If free space is found in one of the ovens the same cooking task is spawned like from the assembly line.
If there is no free space in the stores and in the ovens, assembly lines halt until some of the resources is free again.

Product(2,1) in the printed output means product with size 2 with cooking time 1s
