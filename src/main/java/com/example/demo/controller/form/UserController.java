package com.example.demo.controller.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.demo.service.Data.NE4JDB;
import com.example.demo.service.dto.PersonDTO;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import lombok.var;

@Controller
@RestController
public class UserController {
    
    private PersonDTO personDTO;//usuario actual
    private Warning warnin = new Warning();
    private final NE4JDB base = new NE4JDB("bolt://localhost:11004","neo4j","1234");

    @GetMapping("/user/new")
    public ModelAndView newUser(){//registrar nuevo usuario
        var params = new HashMap<String, Object>();
        PersonDTO user = new PersonDTO();
        params.put("person", user);
        params.put("warning", this.warnin);
        return new ModelAndView("register",params);
    }
    @PostMapping("/user/save")
    public ModelAndView saveUser(PersonDTO personDTO){//guardar nuevo usuario
        if(personDTO.getName().length() == 0){
            this.warnin = new Warning("No puede dejar campos en blanco");
            return new ModelAndView ("redirect:/user/new");
        }
        if(personDTO.getPassword().length() < 10){
            this.warnin = new Warning("La contraseña debe de tener mas de 10 caracteres");
            return new ModelAndView ("redirect:/user/new");
        }
        if(personDTO.getUsername().length() == 0){
            this.warnin = new Warning("No puede dejar campos en blanco");
            return new ModelAndView ("redirect:/user/new");
        }
        
        if(base.usernamaeTaken(personDTO.getUsername())){
            this.warnin = new Warning("Usuario no disponible");
            return new ModelAndView ("redirect:/user/new");
        }
        
        boolean success = base.AddUser(personDTO);
        if (success != true){
            this.warnin = new Warning("NO SE PUDO GUARDAR EL USUARIO CORRECTAMENTE");
            return new ModelAndView ("redirect:/user/new");
        }
        this.personDTO = personDTO;
        this.warnin = new Warning("");
        return new ModelAndView ("redirect:/user/Register2nd");
    }
    @GetMapping("/user/Register2nd")
    public ModelAndView Register2nd(){
        var params = new HashMap<String, Object>();
        params.put("listQuality",base.getQUALITIESNOTLIKED(this.personDTO.getUsername()));
        params.put("listQuality2",base.getQUALITIESNOTHAS(this.personDTO.getUsername()));
        params.put("listPlaces",base.getPlaces(this.personDTO.getUsername()));
        params.put("listGender",base.getGENDER(this.personDTO.getUsername(),1));
        params.put("listGender2",base.getGENDER(this.personDTO.getUsername(),0));
        params.put("warning", this.warnin);
        return new ModelAndView ("registerPriority",params);
    }
    @GetMapping(value = "/user/addlikes/{name}")
    public ModelAndView qualityLiked(@ModelAttribute("name") String id){ //crear relacion entre el usuario y la cualidad gustada
        base.addLiked(this.personDTO.getUsername(), id);
        return new ModelAndView("redirect:/user/Register2nd");
    }
    @GetMapping(value = "/user/addhas/{name}")
    public ModelAndView qualityHAS(@ModelAttribute("name") String id){ //crear relacion entre el usuario y la cualidad gustada
        base.addHAS(this.personDTO.getUsername(), id);
        return new ModelAndView("redirect:/user/Register2nd");
    }
    @GetMapping(value = "/user/lives/{name}")
    public ModelAndView Lives(@ModelAttribute("name") String id){ //crear relacion entre el usuario y la cualidad gustada
        base.Lives(this.personDTO.getUsername(), id);
        return new ModelAndView("redirect:/user/Register2nd");
    }
    @GetMapping(value = "/user/is/{name}")
    public ModelAndView is(@ModelAttribute("name") String id){ //crear relacion entre el usuario y la cualidad gustada
        base.IS(this.personDTO.getUsername(), id);
        return new ModelAndView("redirect:/user/Register2nd");
    }
    @GetMapping(value = "/user/wants/{name}")
    public ModelAndView want(@ModelAttribute("name") String id){ //crear relacion entre el usuario y la cualidad gustada
        base.WANTS(this.personDTO.getUsername(), id);
        return new ModelAndView("redirect:/user/Register2nd");
    }
    @GetMapping("/user/endsave")
    public ModelAndView endsave(){
        switch(base.validateRegisters(this.personDTO.getUsername())){
            case 1:{
                this.warnin = new Warning("No haz seleccionado el departamento");
                return new ModelAndView("redirect:/user/Register2nd");
            }
            case 2:{
                this.warnin = new Warning("Selecciona por lo menos una cualidad que te describa");
                return new ModelAndView("redirect:/user/Register2nd");
            }
            case 3:{
                this.warnin = new Warning("Selecciona por lo menos una cualidad que te guste");
                return new ModelAndView("redirect:/user/Register2nd");
            }
            case 4:{
                this.warnin = new Warning("No haz seleccionado tu sexo");
                return new ModelAndView("redirect:/user/Register2nd");
            }
            case 5:{
                this.warnin = new Warning("No haz seleccionado tu sexo de interes");
                return new ModelAndView("redirect:/user/Register2nd");
            }
        }
        this.warnin = new Warning("");
        return new ModelAndView("redirect:/user/dashboard");
    }

    @GetMapping("/user/sign")
    public ModelAndView logUser(){//inicar sesion
        var params = new HashMap<String, Object>();
        PersonDTO user = new PersonDTO();
        params.put("person", user);
        params.put("warning", this.warnin);
        return new ModelAndView("login",params);
    }
    @PostMapping("/user/login")
    public ModelAndView loginUser(PersonDTO personDTO){//inicio de sesion 
        if(personDTO.getPassword().length() < 10){
            this.warnin = new Warning("La contraseña debe de tener mas de 10 caracteres");
            return new ModelAndView ("redirect:/user/sign");
        }
        if(personDTO.getUsername().length() == 0){
            this.warnin = new Warning("No puede dejar campos en blanco");
            return new ModelAndView ("redirect:/user/sign");
        }
        PersonDTO person = base.Login(personDTO.getUsername());
        if(person == null){
            this.warnin = new Warning("usuario no encontrado");
            return new ModelAndView ("redirect:/user/sign");
        }
        if(!personDTO.getPassword().equals(person.getPassword())){
            this.warnin = new Warning("La contraseña no concuerda");
            return new ModelAndView ("redirect:/user/sign");
        }
        this.personDTO = personDTO;
        this.warnin = new Warning("");
        return new ModelAndView("redirect:/user/dashboard");
    }

    @GetMapping("/user/dashboard")
    public ModelAndView dashboard(){
        if(this.personDTO.empty()){
            this.personDTO.setTeammates(base.getRecomendations(this.personDTO.getUsername()));
        }
        var params = new HashMap<String, Object>();
        params.put("listRoles", this.personDTO.getTeammates() );
        return new ModelAndView("userMainPanel", params);
    }
    @GetMapping("/user/recomendation")
    public ModelAndView recomendation(){
        return new ModelAndView("redirect:/user/dashboard");
    }

    @GetMapping("/user/addrecomend/{username}")
    public ModelAndView addUser(@ModelAttribute("username") String id)//solicitarle al MVC ek campo de nombre
    {   
        this.personDTO.QuitRecomendations(id);
        base.AddFriend(this.personDTO.getUsername(), id);
        return new ModelAndView("redirect:/user/dashboard");
    }
    @GetMapping("/user/deleterecomend/{username}")
    public ModelAndView popUser(@ModelAttribute("username") String id)//solicitarle al MVC el campo de nombre
    {
        this.personDTO.QuitRecomendations(id);
        return new ModelAndView("redirect:/user/dashboard");
    }
    @GetMapping("/user/friends")
    public ModelAndView friends(){
        var params = new HashMap<String, Object>();
        params.put("listRoles", base.getFriends(this.personDTO.getUsername()) );
        return new ModelAndView("userFriendships",params);
    }
    @GetMapping("/user/deletefriend/{username}")
    public ModelAndView deleteUser(@ModelAttribute("username") String id)//solicitarle al MVC el campo de nombre
    {
        base.DeleteFriend(this.personDTO.getUsername(), id);
        return new ModelAndView("redirect:/user/friends");
    }
    @GetMapping("/user/logout")
    public ModelAndView logout(){
        this.personDTO = null; //eliminar informacion de sesion temporal}
        return new ModelAndView ("main"); //regresar a inicio de sesion :p
    }
    
}
    


