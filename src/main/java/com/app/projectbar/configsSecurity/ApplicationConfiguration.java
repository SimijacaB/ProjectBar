package com.app.projectbar.configsSecurity;

import com.app.projectbar.infra.repositories.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
@Configuration
@RequiredArgsConstructor
public class ApplicationConfiguration {

    private final IUserRepository userRepository;

    @Bean//Declara que el método produce un bean que debe ser administrado por el contenedor de Spring.
    //UserDetailsService es una interfaz de Spring Security que define un solo método, loadUserByUsername(String username)
        // Este método se utiliza para cargar los detalles del usuario (como el nombre de usuario, contraseña, roles, etc.) desde una fuente de datos.
    UserDetailsService userDetailsService() {
        return username -> (UserDetails) userRepository.findByEmail(username)//Esta es una expresión lambda que implementa el método loadUserByUsername de UserDetailsService.
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    //BCryptPasswordEncoder : es una clase proporcionada por Spring Security que implementa la interfaz PasswordEncoder.
        // Se utiliza para cifrar (hash) las contraseñas de los usuarios antes de almacenarlas en la base de datos.
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();//Crea una nueva instancia de BCryptPasswordEncoder.
    }

    @Bean
    //AuthenticationManager es una interfaz central en Spring Security que define el contrato para autenticar usuarios.
    // Se encarga de procesar las solicitudes de autenticación y devolver un objeto Authentication que indica si la autenticación fue exitosa o no.
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager(); //Este método obtiene el AuthenticationManager configurado automáticamente por Spring Security,
                                                                            // basado en la configuración global de seguridad de la aplicación (como detalles de usuario, proveedores de autenticación, etc.).
    }

    @Bean
    //Este método define un AuthenticationProvider personalizado que se utiliza para autenticar usuarios mediante la verificación de sus credenciales contra un almacén de datos
        // (como una base de datos) y cifrando sus contraseñas.
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        //DaoAuthenticationProvider es una implementación de AuthenticationProvider que utiliza un UserDetailsService para cargar los detalles del usuario y un PasswordEncoder
        // para verificar las contraseñas.

        authProvider.setUserDetailsService(userDetailsService());//Configura el DaoAuthenticationProvider para que use un UserDetailsService específico para cargar los detalles del usuario.
        authProvider.setPasswordEncoder(passwordEncoder());//Configura el DaoAuthenticationProvider para que use un PasswordEncoder específico para comparar la contraseña ingresada por
                                                                                                // el usuario con la contraseña almacenada.
        return authProvider;
    }

}
