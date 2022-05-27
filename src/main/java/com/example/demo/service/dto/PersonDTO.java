/*
OBJETO DE TRANSFERENCIA ENTRE BASE DE DATOS Y PAGINA
*/
package com.example.demo.service.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class PersonDTO {
    private String name;
    private String username;
    private String password;
    private List<PersonDTO> teammates = new ArrayList<PersonDTO>();
    public PersonDTO(String username,String name) {
        this.name = name;
        this.username = username;
    }

    public void QuitRecomendations(String username) { //remover una recomendacion del listado
        for(int i = 0; i < teammates.size() ; i++) {
            if(teammates.get(i).getUsername().equals(username)){
                teammates.remove(i);
            }
        }
    }

    public boolean empty() {
        return this.teammates.isEmpty();
    }
    

}
