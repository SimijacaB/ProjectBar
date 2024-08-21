package com.app.projectbar.application.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    public JwtService() {
        System.out.println("JwtService construido");
    }

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    /*
   extractUsername(String token) es un método que se utiliza para extraer
   el nombre de usuario (username) de un token JWT (JSON Web Token).
   IMPORTANTE: Los tokens JWT se utilizan para autenticar solicitudes en
   aplicaciones, y suelen contener información útil en sus "claims" (reclamaciones),
   como el nombre de usuario, roles, y otras propiedades.
    */
    public String extractUsername(String token) {
        //return extractClaim(token, Claims::getSubject);

        String username = extractClaim(token, Claims::getSubject);
        System.out.println("Extraído username: " + username);
        return username;
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        //Este método, que se llama internamente dentro de extractClaim, probablemente toma el token JWT, lo decodifica y extrae todos los claims como un objeto Claims. (Método definido dentro de esta misma clase)
        final Claims claims = extractAllClaims(token);
        System.out.println("Claims extraídos: " + claims);
        //Claims es una interfaz de io.jsonwebtoken que representa todos los claims contenidos en un token JWT.
        return claimsResolver.apply(claims);
        //Una vez que tienes el objeto Claims, la función claimsResolver se aplica a ese objeto para extraer un claim específico.
    }

    /*
    Este método es responsable de generar un token JWT para un usuario autenticado.
    Se utiliza cuando un usuario inicia sesión correctamente y necesitas crear un token JWT que represente la sesión de ese usuario.
     */
    public String generateToken(UserDetails userDetails) {
        //El método no genera el token directamente, sino que delega esta tarea a otro método sobrecargado de generateToken que
        // acepta dos parámetros: un mapa de claims adicionales y el objeto UserDetails.
        System.out.println("Generando token para: " + userDetails.getUsername());
        System.out.println("prueba desde jwtService");
        return generateToken(new HashMap<>(), userDetails);
    }

    //Método llamado en el método anterior, por qué? Hacerlo de está manera modulariza el código.
    //Nota: Revisar Chatgpt, ahí explica más a fondo.
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    //El método buildToken es responsable de construir un token JWT utilizando la biblioteca jjwt.
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        System.out.println("Construyendo token con claims: " + extraClaims);
        System.out.println("Detalles del usuario: " + userDetails);
        System.out.println("Fecha de expiración: " + new Date(System.currentTimeMillis() + expiration));
        return Jwts
                .builder()
                /*
                setClaims: Establece los claims (reclamaciones) personalizados que se van a incluir en el token JWT.
                Los claims son pares clave-valor que transportan información sobre el usuario o cualquier otro dato relevante.
                 */
                .setClaims(extraClaims)
                //setSubject: Establece el "subject" del token JWT, que en este caso es el nombre de usuario (username).
                .setSubject(userDetails.getUsername())
                // setIssuedAt: Establece la fecha y hora en la que se emitió el token.
                .setIssuedAt(new Date(System.currentTimeMillis()))
                //setExpiration: Establece la fecha y hora de expiración del token. Este valor se calcula sumando un periodo (en milisegundos) al tiempo actual.
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                //signWith: Firma el token utilizando una clave secreta (getSignInKey()) y un algoritmo de firma (HS256 en este caso, que es HMAC con SHA-256).
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                //compact: Finaliza la construcción del token y lo convierte en su representación compacta, que es la cadena de texto (string) que se devuelve.
                .compact();
        //NOTA: La importancia de cada línea de código, se encuentra adjunta al link de Chatgpt. Revisarla por favor.

    }

    //isTokenValid: se utiliza para validar si un token JWT (JSON Web Token) es válido para un usuario específico. Es llamado en la Clase Filter
    public boolean isTokenValid(String token, UserDetails userDetails) {
//        final String username = extractUsername(token);//Este método extrae el nombre de usuario (username) del token JWT.
//        //equals: Compara el nombre de usuario extraído del token con el nombre de usuario almacenado en el objeto UserDetails.
//
//        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);//Llama a otro método isTokenExpired(token) que verifica si el token JWT ha expirado (definido dentro de esta misma clase).
//
        final String username = extractUsername(token);
        boolean isValid = (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        System.out.println("Token válido: " + isValid + ", Username: " + username);
        return isValid;
    }

    /*
    Este método  extrae la fecha de expiración del token JWT.
    La fecha de expiración es un claim estándar en un token JWT y se almacena bajo la clave exp.
    (extractExpiration: método definido dentro de esta misma clase).
     */
    private boolean isTokenExpired(String token) {
        Date expirationDate = extractExpiration(token);
        boolean expired = expirationDate.before(new Date());
        System.out.println("Fecha de expiración: " + expirationDate + ", Expirado: " + expired);
        return expired;
        //return extractExpiration(token).before(new Date());
    }

    /*
    Este método tiene como objetivo extraer la fecha de expiración (exp) de un token JWT, que es un Date representando el momento en que el token deja de ser válido.
     */
    private Date extractExpiration(String token) {
        Date expirationDate = extractClaim(token, Claims::getExpiration);
        System.out.println("Fecha de expiración extraída: " + expirationDate);
        return expirationDate;
        //Llama al método extractClaim, definido dentro de esta clase.
        //return extractClaim(token, Claims::getExpiration);//Claims::getExpiration: Esta es una referencia a un
        // método que pertenece a la clase Claims de la biblioteca io.jsonwebtoken.
                                                                                    // El método getExpiration devuelve la fecha de expiración del token.
    }

    /*
    Se encarga de extraer todos los claims (reclamaciones) de un token JWT.
     */
    private Claims extractAllClaims(String token) {
//        return Jwts
//                .parserBuilder() //Crea un nuevo objeto JwtParserBuilder, que es utilizado para construir un objeto JwtParser.
//                .setSigningKey(getSignInKey())//Establece la clave secreta (signing key) que se usará para verificar la firma del token JWT. (Recibe como argumento la Key del método definido en esta misma clase)
//                .build()// Construye un objeto JwtParser utilizando la configuración establecida (incluyendo la clave de firma).
//                .parseClaimsJws(token)//Decodifica el token JWT proporcionado como parámetro. Este método verifica la firma del token utilizando la clave proporcionada y decodifica su contenido.
//                .getBody();//Extrae el cuerpo del token JWT, que contiene los claims.
        Claims claims = Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        System.out.println("Claims extraídos del token: " + claims);
        return claims;
    }

    /*
    Este método se encarga de generar una clave criptográfica (Key) a partir de una clave secreta codificada en Base64.
    Esta clave es utilizada para firmar y verificar tokens JWT en la aplicación.
     */
    private Key getSignInKey() {
//        byte[] keyBytes = Decoders.BASE64.decode(secretKey);//Decodifica la cadena secretKey (previamente definida en la clase) desde Base64 a un arreglo de bytes (byte[]).
//        return Keys.hmacShaKeyFor(keyBytes);//Crea una clave HMAC-SHA (HMAC con SHA-256) utilizando el arreglo de bytes decodificado.
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        Key key = Keys.hmacShaKeyFor(keyBytes);
        System.out.println("Clave de firma generada: " + key);
        return key;
    }

}
