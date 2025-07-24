## Controle de notificações

Esse app permite que você controle quando outros aplicativos podem emitir notificações com base em regras que consideram dias e horários, além de condições como se a notificação contém ou não determinada palavra-chave.

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


## Capturas de Tela

<p align="center">
     <img src="https://github.com/user-attachments/assets/9a76c384-d13b-4668-9f59-f15a38f879b3" width="320">
     <img src="https://github.com/user-attachments/assets/d9e11998-fd2b-42e6-867b-9c230814527a" width="320">
     <img src="https://github.com/user-attachments/assets/9d4bb920-7b69-412c-820a-5d1334ef1d64" width="320">
     <img src="https://github.com/user-attachments/assets/a154c6e8-4b37-48ff-b55e-50f27986cfb6" width="320">
     <img src="https://github.com/user-attachments/assets/78a155f7-f085-4936-8d6e-335dfff2824f" width="320">
     <img src="https://github.com/user-attachments/assets/b6a19855-ed50-4be0-87c2-8f92e01559a1" width="320">
</p>

