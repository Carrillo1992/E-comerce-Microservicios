package com.dcarrillo.ecomerce.userservice.service; // Asegúrate de que coincida con tu paquete

import com.dcarrillo.ecomerce.userservice.dto.UserRegisterDTO;
import com.dcarrillo.ecomerce.userservice.entity.Role;
import com.dcarrillo.ecomerce.userservice.entity.User;
import com.dcarrillo.ecomerce.userservice.repository.RoleRepository;
import com.dcarrillo.ecomerce.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRegisterDTO userRegisterDTO;
    private User user;
    private Role userRole;

    @BeforeEach
    void setup() {
        userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setName("Test User");
        userRegisterDTO.setEmail("test@example.com");
        userRegisterDTO.setPassword("password123");

        userRole = new Role("ROLE_USER");
        userRole.setId(1L);

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setPasswordHash("$2a$10$hashedpasswordexample");
        user.setRoles(Set.of(userRole));
    }

    @Test
    @DisplayName("Debería registrar un nuevo usuario exitosamente")
    void testUserRegister_Success() {
        // GIVEN
        given(userRepository.existsByEmail(userRegisterDTO.getEmail())).willReturn(false);
        given(passwordEncoder.encode(userRegisterDTO.getPassword())).willReturn("$2a$10$hashedpasswordexample");
        given(roleRepository.findByName("ROLE_USER")).willReturn(Optional.of(userRole));
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        User savedUser = userService.userRegister(userRegisterDTO);

        // THEN
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo(userRegisterDTO.getEmail());
        assertThat(savedUser.getPasswordHash()).isEqualTo("$2a$10$hashedpasswordexample");
        assertThat(savedUser.getRoles()).contains(userRole);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción si el email ya existe al registrar")
    void testUserRegister_EmailAlreadyExists_ThrowsException() {
        // GIVEN
        given(userRepository.existsByEmail(userRegisterDTO.getEmail())).willReturn(true);

        // WHEN & THEN
        assertThrows(RuntimeException.class, () -> {
            userService.userRegister(userRegisterDTO);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("loadUserByUsername debería devolver UserDetails si el usuario existe")
    void testLoadUserByUsername_UserFound() {
        // GIVEN
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));

        // WHEN
        UserDetails userDetails = userService.loadUserByUsername("test@example.com");

        // THEN
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("test@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("$2a$10$hashedpasswordexample");
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("loadUserByUsername debería lanzar UsernameNotFoundException si el usuario no existe")
    void testLoadUserByUsername_UserNotFound_ThrowsException() {
        // GIVEN
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("nouser@example.com");
        });
    }
}
