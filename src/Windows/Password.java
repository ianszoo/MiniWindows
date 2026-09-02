/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Windows;

import java.util.regex.Pattern;

/**
 *
 * @author Ian Suazo Palao
 */
public class Password {
    private static final String REGEX="^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$";
    private static final Pattern pattern = Pattern.compile(REGEX);
    
    public static void validar(String password) throws PasswordInvalidEsception{
        if (password==null || !pattern.matcher(password).matches()){
            throw new PasswordInvalidEsception(
                "La contraseña debe tener al menos 8 caracteres, incluir una letra mayúscula, un número y un símbolo especial."
            );
        }
    }
}
