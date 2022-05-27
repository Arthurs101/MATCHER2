package com.example.demo.service.Data;
import java.util.ArrayList;
import java.util.List;
import com.example.demo.service.dto.*;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;
import static org.neo4j.driver.Values.parameters;
public class NE4JDB implements AutoCloseable{
    private final Driver driver;
    public NE4JDB( String uri, String user, String password )
    { //coneccion con la base de datos
        driver = GraphDatabase.driver( uri, AuthTokens.basic( user, password ) );
    }
@Override
public void close() throws Exception {
    driver.close(); //cerrar coneccion
}
public void printGreeting(String message )
    {
        try ( Session session = driver.session() )
        {
            String greeting = session.writeTransaction( new TransactionWork<String>()
            {
                @Override
                public String execute( Transaction tx )
                {
                    Result result = tx.run( "CREATE (a:Greeting) " +
                                                     "SET a.message = $message " +
                                                     "RETURN a.message + ', from node ' + id(a)",
                            parameters( "message", message ) );
                    return result.single().get( 0 ).asString();
                }
            } );
            System.out.println( greeting );
        }
    }

public PersonDTO Login( String username){ //obtener el usuario
    try(Session session = driver.session()){
        PersonDTO user = session.readTransaction( new TransactionWork<PersonDTO>() 
        {
            @Override
        public PersonDTO execute (Transaction tx)
        {
            Result result = tx.run( "MATCH (p:Person{username: \"" + username + "\"}) RETURN p.name, p.username, p.password");
            List<Record> reco = result.list(); //lista de coincidencias
            if (reco.isEmpty()){
                return null;
            }else{
                PersonDTO usertemp = new PersonDTO();
                usertemp.setUsername(username);
                usertemp.setPassword(reco.get(0).get("p.password").asString());
                usertemp.setName(reco.get(0).get("p.name").asString());
                return usertemp;
            }
        }
        });
        return user;

    }
   
}
public Boolean usernamaeTaken( String username){ //verificar si esta disponible el nombre de usuario
    try(Session session = driver.session()){
        Boolean user = session.readTransaction( new TransactionWork<Boolean>() 
        {
            @Override
        public Boolean execute (Transaction tx)
        {
            Result result = tx.run( "MATCH (p:Person {username: \"" + username + "\"}) RETURN p");
            List<Record> reco = result.list(); //lista de coincidencias
            if (reco.isEmpty()){
                return false;
            }else{
                return true;
            }
        }
        });
        return user;
    }
    
}
public Boolean AddUser(PersonDTO Person){ //Añadir el usuario true:añadido correctamente
    try(Session session = driver.session()){
        Boolean user = session.writeTransaction( new TransactionWork<Boolean>() 
        {
            @Override
        public Boolean execute (Transaction tx)
        {
            String username = Person.getUsername();
            String name = Person.getName();
            String password = Person.getPassword();
            tx.run("CREATE (:Person {username: \"" + username + "\" , name: \"" + name + "\" , password: \"" + password + "\"})");
            Result result = tx.run( "MATCH (p:Person {username: \"" + username + "\"}) RETURN p");
            List<Record> reco = result.list(); //lista de coincidencias
            if (reco.isEmpty()){
                return false;
            }else{
                return true;
            }
        }
        });
        return user;
    }
}
public void addFriend(String username1, String username2){
    
}
    public List<QUALITYDTO> getQUALITIESNOTLIKED(String username){ //obtener el usuario
        try(Session session = driver.session()){
            List<QUALITYDTO> q = session.readTransaction( new TransactionWork<List<QUALITYDTO>>() 
            {
                @Override
            public List<QUALITYDTO> execute (Transaction tx)
            {
                Result result = tx.run( "MATCH (q:QUALITY) WHERE NOT (:Person {username:\"" + username + "\"})-[:LIKES]->(q)  RETURN q.name");
                List<Record> reco = result.list(); //lista de coincidencias
                List<QUALITYDTO> qualities = new ArrayList<QUALITYDTO>();
                for (int i = 0; i < reco.size(); i++) {
                    //añadir las cualidades al array a retornar
                    qualities.add(new QUALITYDTO(reco.get(i).get("q.name").asString()));
                }
                return qualities;
            }
            });
            return q;
        }
    }

    public List<QUALITYDTO> getQUALITIESNOTHAS(String username){ //obtener el usuario
        try(Session session = driver.session()){
            List<QUALITYDTO> q = session.readTransaction( new TransactionWork<List<QUALITYDTO>>() 
            {
                @Override
            public List<QUALITYDTO> execute (Transaction tx)
            {
                Result result = tx.run( "MATCH (q:QUALITY) WHERE NOT (:Person {username:\"" + username + "\"})-[:HAS]->(q)  RETURN q.name");
                List<Record> reco = result.list(); //lista de coincidencias
                List<QUALITYDTO> qualities = new ArrayList<QUALITYDTO>();
                for (int i = 0; i < reco.size(); i++) {
                    //añadir las cualidades al array a retornar
                    qualities.add(new QUALITYDTO(reco.get(i).get("q.name").asString()));
                }
                return qualities;
            }
            });
            return q;
        }
    }

    public void addLiked(String username, String quality){ //crear relacion entre usuario y cualidad
        try ( Session session = driver.session() )
        {
            session.writeTransaction( new TransactionWork<String>()
            {
                @Override
                public String execute( Transaction tx )
                {
                    tx.run( "MATCH (q:QUALITY{name: \"" +quality + "\"}),(p:Person {username: \"" + username + "\"})  CREATE (p)-[:LIKES]->(q)" );
                    return "";
                }
            } );
            
        }
    }
    public void addHAS(String username, String quality){ //crear relacion entre usuario y cualidad
        try ( Session session = driver.session() )
        {
            session.writeTransaction( new TransactionWork<String>()
            {
                @Override
                public String execute( Transaction tx )
                {
                    tx.run( "MATCH (q:QUALITY{name: \"" +quality + "\"}),(p:Person {username: \"" + username + "\"})  CREATE (p)-[:HAS]->(q)" );
                    return "";
                }
            } );
            
        }
    }
    
    public List<Place> getPlaces(String username){ //obtener el usuario
        try(Session session = driver.session()){
            List<Place> q = session.readTransaction( new TransactionWork<List<Place>>() 
            {
                @Override
            public List<Place> execute (Transaction tx)
            {
                Result result = tx.run( "MATCH (:Person{username: \"" + username + "\"})-[r:LIVES]->(:PLACE) return r");//verificar que no tenga ya seleccionado un lugar
                List<Record> reco = result.list(); //lista de coincidencias
                List<Place> qualities = new ArrayList<Place>();

                if(reco.isEmpty()){

                    Result result2 = tx.run( "MATCH (p:PLACE) RETURN p.name");
                    List<Record> reco2 = result2.list(); //lista de coincidencias
                    for (int i = 0; i < reco2.size(); i++) {
                        //añadir las cualidades al array a retornar
                        qualities.add(new Place(reco2.get(i).get("p.name").asString()));
                    }
                }
                
                return qualities;
            }
            });
            return q;
        }
    }
    public void Lives(String username, String id) {
        try ( Session session = driver.session() )
        {
            session.writeTransaction( new TransactionWork<String>()
            {
                @Override
                public String execute( Transaction tx )
                {
                    tx.run( "MATCH (q:PLACE{name: \"" +id + "\"}),(p:Person {username: \"" + username + "\"})  CREATE (p)-[:LIVES]->(q)" );
                    return "";
                }
            } );
            
        }
    }
    public List<GenderDTO> getGENDER(String username, int mode){ //obtener el usuario
        try(Session session = driver.session()){
            List<GenderDTO> q = session.readTransaction( new TransactionWork<List<GenderDTO>>() 
            {
                @Override
            public List<GenderDTO> execute (Transaction tx)
            {
                switch(mode)
                {
                    case 1 : {
                        Result result = tx.run( "MATCH (:Person{username: \"" + username + "\"})-[r:IS]->(:GENDER) return r");//verificar que no tenga ya seleccionado un lugar
                        List<Record> reco = result.list(); //lista de coincidencias
                        List<GenderDTO> qualities = new ArrayList<>();

                        if(reco.isEmpty()){

                            Result result2 = tx.run( "MATCH (p:GENDER) RETURN p.name");
                            List<Record> reco2 = result2.list(); //lista de coincidencias
                            for (int i = 0; i < reco2.size(); i++) {
                                //añadir las cualidades al array a retornar
                                qualities.add(new GenderDTO(reco2.get(i).get("p.name").asString()));
                            }
                        }
                        return qualities;
                    }
                    case 0 : {
                        Result result = tx.run( "MATCH (:Person{username: \"" + username + "\"})-[r:WANTS]->(:GENDER) return r");//verificar que no tenga ya seleccionado un lugar
                        List<Record> reco = result.list(); //lista de coincidencias
                        List<GenderDTO> qualities = new ArrayList<>();

                        if(reco.isEmpty()){

                            Result result2 = tx.run( "MATCH (p:GENDER) RETURN p.name");
                            List<Record> reco2 = result2.list(); //lista de coincidencias
                            for (int i = 0; i < reco2.size(); i++) {
                                //añadir las cualidades al array a retornar
                                qualities.add(new GenderDTO(reco2.get(i).get("p.name").asString()));
                            }
                        }
                        return qualities;
                    }
                    default:{
                        return null;
                    }
                        
                }              
            }
            });
            return q;
        }
    }
    public void IS(String username, String id) {
        try ( Session session = driver.session() )
        {
            session.writeTransaction( new TransactionWork<String>()
            {
                @Override
                public String execute( Transaction tx )
                {
                    tx.run( "MATCH (q:GENDER{name: \"" +id + "\"}),(p:Person {username: \"" + username + "\"})  CREATE (p)-[:IS]->(q)" );
                    return "";
                }
            } );
            
        }
    }
    public void WANTS(String username, String id) {
        try ( Session session = driver.session() )
        {
            session.writeTransaction( new TransactionWork<String>()
            {
                @Override
                public String execute( Transaction tx )
                {
                    tx.run( "MATCH (q:GENDER{name: \"" +id + "\"}),(p:Person {username: \"" + username + "\"})  CREATE (p)-[:WANTS]->(q)" );
                    return "";
                }
            } );
            
        }
    }
    public int validateRegisters(String username){//validar el registro del nuevo usuario 0 no hay errores, registio correcto -1 error inesperado
        try(Session session = driver.session()){
            int q = session.readTransaction( new TransactionWork<Integer>() 
            {
                @Override
            public Integer execute (Transaction tx)
            {
                Result result = tx.run( "MATCH (:Person{username: \"" + username + "\"})-[r:LIVES]->(:PLACE) return r");//verificar que no tenga ya seleccionado un lugar
                List<Record> reco = result.list(); //lista de coincidencias
                if(reco.isEmpty()){
                    return 1;
                }
                result = tx.run( "MATCH (:Person{username: \"" + username + "\"})-[r:HAS]->(:QUALITY) return r");//verificar que no tenga ya seleccionado un lugar
                reco = result.list(); //lista de coincidencias
                if(reco.isEmpty()){
                    return 2;
                }
                result = tx.run( "MATCH (:Person{username: \"" + username + "\"})-[r:LIKES]->(:QUALITY) return r");//verificar que no tenga ya seleccionado un lugar
                reco = result.list(); //lista de coincidencias
                if(reco.isEmpty()){
                    return 3;
                }
                result = tx.run( "MATCH (:Person{username: \"" + username + "\"})-[r:IS]->(:GENDER) return r");//verificar que no tenga ya seleccionado un lugar
                reco = result.list(); //lista de coincidencias
                if(reco.isEmpty()){
                    return 4;
                }
                result = tx.run( "MATCH (:Person{username: \"" + username + "\"})-[r:WANTS]->(:GENDER) return r");//verificar que no tenga ya seleccionado un lugar
                reco = result.list(); //lista de coincidencias
                if(reco.isEmpty()){
                    return 5;
                }
                return 0;
                
               
            }
            });
            return q;
        }
        
        
    }
    public List<PersonDTO> getFriends(String username) {
        try(Session session = driver.session()){
            List<PersonDTO> friends = session.readTransaction( new TransactionWork<List<PersonDTO>>() 
            {
                @Override
            public List<PersonDTO> execute (Transaction tx)
            {
                Result result = tx.run( "MATCH (:Person{username: \"" + username + "\"})-[:KNOWS]->(p:Person) RETURN p.name, p.username");
                List<Record> reco = result.list(); //lista de coincidencias
                List<PersonDTO> tmp = new ArrayList<>();
                for (int i = 0; i <reco.size(); i++){
                    PersonDTO usertemp = new PersonDTO(reco.get(i).get("p.username").asString(),reco.get(i).get("p.name").asString());
                    tmp.add(usertemp);
                }
                   return tmp;
                    
                }
            }
            );
            return friends;
        }
    }
    public void AddFriend(String username, String friend) {
        try ( Session session = driver.session() )
        {
            session.writeTransaction( new TransactionWork<String>()
            {
                @Override
                public String execute( Transaction tx )
                {
                    tx.run( "MATCH (q:Person {username: \"" + friend + "\"}),(p:Person {username: \"" + username + "\"})  CREATE (p)-[:KNOWS]->(q)" );
                    return "";
                }
            } );
            
        }
    }
    public void DeleteFriend(String username, String friend) {
        try ( Session session = driver.session() )
        {
            session.writeTransaction( new TransactionWork<String>()
            {
                @Override
                public String execute( Transaction tx )
                {
                    tx.run( "MATCH (p:Person {username: \"" + username + "\"})-[r:KNOWS]->(q:Person {username: \"" + friend + "\"}) DELETE r" );
                    return "";
                }
            } );
            
        }
    }
    public List<PersonDTO> getRecomendations(String username) {
        try(Session session = driver.session()){
            List<PersonDTO> friends = session.readTransaction( new TransactionWork<List<PersonDTO>>() 
            {
                @Override
            public List<PersonDTO> execute (Transaction tx)
            {
                Result result = tx.run( "MATCH (q:Person{username: \"" + username + "\"})-[:LIKES]->(:QUALITY)<-[:HAS]-(p:Person)  Where (q)-[:WANTS]->(:GENDER)<-[:IS]-(p) and not p.username = q.username and not (q)-[:KNOWS]->(p) RETURN DISTINCT  p.name, p.username LIMIT 20");
                List<Record> reco = result.list(); //lista de coincidencias
                List<PersonDTO> tmp = new ArrayList<>();
                for (int i = 0; i <reco.size(); i++){
                    PersonDTO usertemp = new PersonDTO(reco.get(i).get("p.username").asString(),reco.get(i).get("p.name").asString());
                    tmp.add(usertemp);
                }
                result = tx.run( "MATCH (q:Person{username: \"" + username + "\"})-[:LIKES]->(:QUALITY)<-[:HAS]-(p:Person)  Where (q)-[:LIVES]->(:PLACE)<-[:LIVES]-(p) and not p.username = q.username and not (q)-[:KNOWS]->(p) RETURN DISTINCT  p.name, p.username LIMIT 10");
                reco = result.list(); //lista de coincidencias
                for (int i = 0; i <reco.size(); i++){
                    PersonDTO usertemp = new PersonDTO(reco.get(i).get("p.username").asString(),reco.get(i).get("p.name").asString());
                    tmp.add(usertemp);
                }
                   return tmp;
                    
                }
            }
            );
            return friends;
        }
    }
}
