/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.realestate.mrhouse.Services;
import com.realestate.mrhouse.Entities.Users;
import com.realestate.mrhouse.Exceptions.MyException;
import com.realestate.mrhouse.Relations.Rol;
import com.realestate.mrhouse.Repositories.UserRepository;
import com.realestate.mrhouse.smtp.EmailServices;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import static com.sun.jmx.snmp.SnmpStatusException.readOnly;



/**
 *
 * @author 2171584201008
 */
@Service
public class UserService implements UserDetailsService {

    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailServices emailService;
    

    @Transactional
    public void register(String name, String email, String password, String password2) throws MyException {
        validation(name, email, password, password2);

        Users user = new Users();

        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        //user.setPassword(new BCryptPasswordEncoder().encode(password));
        user.setRol(Rol.USER);

        userRepository.save(user);

        emailService.sendEmail(email, "bienvenido/a- " + name,
                "su usuario fue creado correctamente y vinculado al correo electronico " + email);
    }

    private void validation(String name, String email, String password, String password2) throws MyException {
        if (name.isEmpty() || name == null) {
            throw new MyException("el nombre no puede ser nulo o estar vacío");
        }
        if (email.isEmpty() || email == null) {
            throw new MyException("el email no puede ser nulo o estar vacio");
        }
        if (password.isEmpty() || password == null || password.length() <= 5) {
            throw new MyException("La contraseña no puede estar vacía, y debe tener más de 5 dígitos");
        }

        if (!password.equals(password2)) {
            throw new MyException("Las contraseñas ingresadas deben ser iguales");
        }

    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

        Users u = userRepository.searchByEmail(email);

        if (u != null) {

            List<GrantedAuthority> permissions = new ArrayList();
            GrantedAuthority p = new SimpleGrantedAuthority("ROLE_" + u.getRol().toString());
            permissions.add(p);
/*
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession(true);
            session.setAttribute("usuariosession", u);
*/
            return new User(u.getEmail(), u.getPassword(),permissions);

        } else {
            return null;
        }

    }

}
