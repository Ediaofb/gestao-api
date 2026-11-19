package br.com.lydero.gestao_api.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    public static final String TIPO_ADMIN = "ADMIN";
    public static final String TIPO_COMUM = "COMUM";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(nullable = false, length = 120)
    private String nome;

    /** E-mail e o login. Sempre gravado em minusculas. */
    @Column(nullable = false, length = 150)
    private String email;

    /** Hash BCrypt. Nunca guardar nem devolver a senha pura. */
    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;

    @Column(nullable = false, length = 20)
    private String tipo = TIPO_COMUM;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "ultimo_acesso")
    private LocalDateTime ultimoAcesso;

    public boolean isAdmin() {
        return TIPO_ADMIN.equals(tipo);
    }
}
