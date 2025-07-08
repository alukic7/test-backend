package com.example.test_backend.configuration;

import com.example.test_backend.accounts.AccountController;
import com.example.test_backend.accounts.AccountService;
import com.example.test_backend.accounts.repository.AccountRepository;
import com.example.test_backend.accounts.repository.AccountRepositoryImpl;
import com.example.test_backend.filters.CorsFilter;
import com.example.test_backend.filters.CsrfFilter;
import com.example.test_backend.filters.SessionFilter;
import com.example.test_backend.session.repository.*;
import com.example.test_backend.transactions.TransactionController;
import com.example.test_backend.transactions.TransactionService;
import com.example.test_backend.transactions.repository.TransactionRepository;
import com.example.test_backend.transactions.repository.TransactionRepositoryImpl;
import com.example.test_backend.user.UserController;
import com.example.test_backend.user.UserService;
import com.example.test_backend.user.repository.*;
import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import javax.sql.DataSource;

@Configuration
public class AppConfig {
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setUrl(System.getenv("DB_URL"));
        ds.setUsername(System.getenv("DB_USER"));
        ds.setPassword(System.getenv("DB_PASSWORD"));
        return ds;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserRepository userRepository(JdbcTemplate jdbc) {
        return new UserRepositoryImpl(jdbc);
    }

    @Bean
    public SessionRepository sessionRepository(JdbcTemplate jdbc) {
        return new SessionRepositoryImpl(jdbc);
    }

    @Bean
    public UserService userService(UserRepository ur, SessionRepository sr, PasswordEncoder enc) {
        return new UserService(ur, sr, enc);
    }

    @Bean
    public UserController userController(UserService service) {
        return new UserController(service);
    }

    @Bean public AccountRepository accountRepository(JdbcTemplate jdbc) {
        return new AccountRepositoryImpl(jdbc);
    }

    @Bean public AccountService accountService(AccountRepository repo) {
        return new AccountService(repo);
    }

    @Bean public AccountController accountController(AccountService service) {
        return new AccountController(service);
    }

    @Bean
    public TransactionRepository transactionRepository(JdbcTemplate jdbc) {
        return new TransactionRepositoryImpl(jdbc);
    }

    @Bean
    public TransactionService transactionService(TransactionRepository tr, AccountRepository ar) {
        return new TransactionService(tr, ar);
    }

    @Bean
    public TransactionController transactionController(TransactionService ts) {
        return new TransactionController(ts);
    }

    @Bean
    public SessionFilter sessionFilter(SessionRepository sr) {
        return new SessionFilter(sr);
    }

    @Bean
    public FilterRegistrationBean<Filter> sessionFilterRegistration(SessionFilter f) {
        FilterRegistrationBean<Filter> reg = new FilterRegistrationBean<>(f);
        reg.addUrlPatterns("/api/*");
        reg.setOrder(2);
        return reg;
    }

    @Bean
    public FilterRegistrationBean<Filter> corsFilterRegistration() {
        FilterRegistrationBean<Filter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new CorsFilter());
        reg.addUrlPatterns("/*");
        reg.setOrder(0);
        return reg;
    }

    @Bean
    public FilterRegistrationBean<Filter> csrfFilterRegistration(SessionRepository repo) {
        FilterRegistrationBean<Filter> reg = new FilterRegistrationBean<>(new CsrfFilter(repo));
        reg.addUrlPatterns("/api/*");
        reg.setOrder(1);
        return reg;
    }
}
