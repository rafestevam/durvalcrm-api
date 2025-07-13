# =================================================================
# ESTÁGIO 1: BUILDER
# Usamos a imagem completa do Eclipse Temurin com JDK para compilar a aplicação.
# =================================================================
FROM eclipse-temurin:17-jdk-focal as builder

# Define o diretório de trabalho
WORKDIR /app

# Copia os arquivos necessários para o Maven baixar as dependências
COPY mvnw .
COPY .mvn ./.mvn
COPY pom.xml .

# Executa o download das dependências para aproveitar o cache de camadas do Docker
RUN ./mvnw dependency:go-offline

# Copia o restante do código-fonte da aplicação
COPY src ./src

# Compila a aplicação e gera o JAR. Pulamos os testes que já rodam no pipeline de CI.
RUN ./mvnw package -DskipTests


# =================================================================
# ESTÁGIO 2: PRODUCTION
# Usamos uma imagem JRE mínima e segura do Temurin para rodar a aplicação.
# Esta imagem é menor e tem menos vulnerabilidades.
# =================================================================
FROM eclipse-temurin:17-jre-focal

# Define o diretório de trabalho
WORKDIR /app

# Cria um usuário não-root para rodar a aplicação por segurança
RUN groupadd --system appuser && useradd --system --gid appuser appuser
USER appuser

# Copia apenas os artefatos da aplicação do estágio 'builder'
# Isso inclui o JAR da aplicação e as dependências na pasta 'lib'
COPY --from=builder /app/target/quarkus-app/ ./

# Expõe a porta padrão do Quarkus
EXPOSE 8080

# Comando para iniciar a aplicação Java
# Quarkus espera que o comando seja executado a partir do diretório que contém o JAR
CMD ["java", "-jar", "quarkus-run.jar"]