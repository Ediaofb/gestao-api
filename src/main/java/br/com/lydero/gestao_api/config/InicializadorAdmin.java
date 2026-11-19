package br.com.lydero.gestao_api.config;

import br.com.lydero.gestao_api.empresa.Empresa;
import br.com.lydero.gestao_api.empresa.EmpresaRepository;
import br.com.lydero.gestao_api.usuario.Usuario;
import br.com.lydero.gestao_api.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Na primeira execucao (banco sem nenhuma empresa), cria a primeira empresa e o
 * primeiro administrador a partir das variaveis de ambiente BOOTSTRAP_ADMIN_EMAIL e
 * BOOTSTRAP_ADMIN_SENHA. Se elas nao estiverem definidas, nao faz nada.
 */
@Component
public class InicializadorAdmin implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InicializadorAdmin.class);

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    private final String nomeEmpresa;
    private final String nomeAdmin;
    private final String emailAdmin;
    private final String senhaAdmin;

    public InicializadorAdmin(EmpresaRepository empresaRepository,
                              UsuarioRepository usuarioRepository,
                              PasswordEncoder passwordEncoder,
                              @Value("${app.bootstrap.empresa-nome:Minha Empresa}") String nomeEmpresa,
                              @Value("${app.bootstrap.admin-nome:Administrador}") String nomeAdmin,
                              @Value("${app.bootstrap.admin-email:}") String emailAdmin,
                              @Value("${app.bootstrap.admin-senha:}") String senhaAdmin) {
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.nomeEmpresa = nomeEmpresa;
        this.nomeAdmin = nomeAdmin;
        this.emailAdmin = emailAdmin;
        this.senhaAdmin = senhaAdmin;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (empresaRepository.count() > 0) {
            return;
        }
        if (emailAdmin == null || emailAdmin.isBlank() || senhaAdmin == null || senhaAdmin.isBlank()) {
            log.info("Nenhuma empresa cadastrada. Para criar o primeiro administrador, defina as variaveis "
                    + "BOOTSTRAP_ADMIN_EMAIL e BOOTSTRAP_ADMIN_SENHA e reinicie a aplicacao.");
            return;
        }
        if (senhaAdmin.length() < 8) {
            log.warn("BOOTSTRAP_ADMIN_SENHA precisa ter pelo menos 8 caracteres. Administrador nao foi criado.");
            return;
        }

        Empresa empresa = new Empresa();
        empresa.setRazaoSocial(nomeEmpresa);
        empresa = empresaRepository.save(empresa);

        Usuario admin = new Usuario();
        admin.setEmpresaId(empresa.getId());
        admin.setNome(nomeAdmin);
        admin.setEmail(emailAdmin.trim().toLowerCase());
        admin.setSenhaHash(passwordEncoder.encode(senhaAdmin));
        admin.setTipo(Usuario.TIPO_ADMIN);
        usuarioRepository.save(admin);

        log.info("Primeira empresa e administrador criados (login: {}). "
                + "Depois de entrar, remova BOOTSTRAP_ADMIN_SENHA da configuracao.", admin.getEmail());
    }
}
