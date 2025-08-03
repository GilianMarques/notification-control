## Controle de notificações

Esse app permite que você controle quando outros aplicativos podem emitir notificações com base em regras que consideram dias e horários e condições, além de permitir adiar para uma data específica ou ocultar notificações, mesmo as persistentes.

## Problema:

É normal perder o foco por causa das notificações que chegam no celular. Para quem usa o mesmo dispositivo para a vida pessoal e profissional, também é normal ser incomodado fora do horário de expediente ou até nos fins de semana com notificações de aplicativos de trabalho.

## Solução:

Este app adia ou bloqueia notificações de qualquer outro app com base nas configurações do usuário, que pode definir os dias, horários e/ou condições em que determinado app pode (ou não) emitir notificações.
Notificações recebidas durante o período de bloqueio são ocultadas até o próximo período de desbloqueio, quando então são reemitidas como se tivessem acabado de chegar.

Essa ferramenta concede ao usuário a capacidade de eliminar distrações de maneira centralizada e com facilidade. Veja o que dá pra fazer na prática:

* Impedir que redes sociais, mensageiros e jogos tirem o foco durante o trabalho/estudos
* Impedir que notificações de trabalho incomodem durante as folgas e períodos de descanso
* Adiar notificações para uma data específica
* Ocultar notificações persistentes
* Bloquear notificações específicas com base em condições
* Bloquear todas as notificações de um aplicativo, mas mantendo um histórico para consultas eventuais


## Tecnologias e arquitetura

Usei **Clean Architecture** e **MVVM** para manter o código modular e escalável.
**Coroutines** e **mutex** para gerenciar tarefas assíncronas e interagir com a interface de forma segura.
O layout foi desenvolvido em **XML**, e o **framework Transitions** adiciona animações atraentes e suaves.
Integrei **Flow** com **Room** para criar uma interface reativa e facilitar a combinação de dados de múltiplas fontes **DataStore** para armazenar preferências e **LRU** para cache de vetores e bitmaps in-memory.
O **AlarmManager** foi utilizado para manter o serviço de controle sempre rodando e emitir notificações de alerta ao término dos períodos de bloqueio.
**Firebase Auth**, **Crashlytics** e **RemoteConfig** também foram usados.
O projeto também conta com **testes unitários** e **instrumentados**.
Por fim, implementei **descrições de conteúdo** para tornar o aplicativo acessível a pessoas com deficiência visual.



## Como rodar o projeto

1.  **Crie uma chave de assinatura (release):**  
    No Android Studio, vá em:  
    `Build > Generate Signed Bundle / APK`  
    Siga o assistente para gerar uma chave `.jks`.
    
2.  **Crie o arquivo `keystore.properties` na raiz do projeto:**  
    Isso é necessário para rodar as versões `staging` e `release` localmente.
    
    Preencha com:
    
    ```properties
    storeFile=C:/Users/Usuario/Documents/GitHub/controle_de_notificacoes.jks  (caminho da sua chave)
    storePassword=senha_keystore
    keyAlias=release  (use o mesmo alias definido durante a criação da chave)
    keyPassword=senha_alias
    
    ```
    
3.  **Configure o Firebase:**
    
    -   Crie uma conta no [Firebase](https://firebase.google.com/). 
        
    -   Crie um novo projeto e adicione um app Android.
        
    -   Inclua as digitais SHA-1 das chaves `debug` (gerada automaticamente pelo Android Studio) e `release` (a que você criou).
        
    -   Baixe o arquivo `google-services.json` e salve-o na pasta `app/`.
        
4.  **Configure o Java:**
    
    O projeto utiliza o `JDK 21`.  
Vá em `Settings > Build, Execution, Deployment > Build Tools > Gradle` e defina `Gradle JDK` para umas das versões  `21.x.x` embarcadas ou instalada na sua máquina. Caso opte por instalar um jdk manualmente, certifique-se de e configurar a variável de ambiente `JAVA_HOME` apontando para o diretório raiz da instalação.

## Capturas de Tela

<p align="center">
    <img src= "https://github.com/user-attachments/assets/f9e13802-0135-4a2a-875a-eff7a8e6f750" width="240">
    <img src= "https://github.com/user-attachments/assets/c2f7a4da-feb7-4bdf-b7ef-71df9da7aef1" width="240">
    <img src= "https://github.com/user-attachments/assets/e3b7cedf-b346-4e2e-8fba-bfcb7af92cbc" width="240">
    <img src= "https://github.com/user-attachments/assets/f27392ca-a24c-4f5f-b223-7f46218662c3" width="240">
    <img src= "https://github.com/user-attachments/assets/0d959ae4-f8aa-4615-a728-002fc64b6aba" width="240">
    <img src= "https://github.com/user-attachments/assets/5b222cb7-2012-4b8e-95bf-5bfe75dc8033" width="240">
    <img src= "https://github.com/user-attachments/assets/82aecc01-66d9-47ad-b8eb-82a7a8f3c5fb" height="500">
    <img src= "https://github.com/user-attachments/assets/b31e876a-254d-4790-91b9-279495fc07b2" height="500">
    <img src= "https://github.com/user-attachments/assets/bcbbb6ae-02e5-411c-8784-5d170aca4d5d" height="500">
</p>






