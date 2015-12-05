# Trabalho de Conclusão de Curso de Ciência da Computação - 2015
Nosso projeto se baseia em uma competição entre Agentes Inteligentes capazes de jogar [Angry Birds](https://www.angrybirds.com/). Temos como objetivo analisar os conceitos básicos para o estudo de *Inteligencia Artificial* aplicada a jogos, assim podemos vivenciar as dificuldades e aprender na prática o que nos foi passado em aula.

## Angry Birds AI Competition
Site da Competição: https://aibirds.org/

A tarefa da competição é basicamente desenvolver um Agente Inteligente que consiga jogar Angry Birds sem a intervenção humana, e tendo como um objetivo mais além fazer com que ele consiga jogar novos leveis melhor do que os melhores jogadores humanos. Isso é bem complicado já que requer que o Agente consiga predizer o que ocorrerá no cenário dado algumas informações, e aprender quais ações podem ser tomadas dada as condições da fase.

O projeto é disponibilizado com um framework de desenvolvimento com 3 componentes básicos:
- Visão Computacional
- Planejador de Trajetória
- Modo de Jogo

O projeto tem 2 modos de jogo,  o modo *Client/Server* e o modo  *StandAlone*, ambos tem o mesmo entry point uma classe chamada *MainEntry* a qual dependendo dos parametros de configuração que passemos instancia o Agente responsável pelo modo.
- Client/Server -> *ClientNaiveAgent*
- StandAlone -> *NaiveAgent*

Os dois jogam do mesmo modo, encontrando os porcos de cada fase e escolhendo um aleatóriamente para ser seu alvo de tiro, não considerando fraquezas das estruturas do jogo vs habilidades ou quantidade de dano dos pássaros.

###  Compilar Projeto
```
ant compile
ant jar
```

###  Executar Projeto
- **Client/Server**

    Neste modo é necessário subir o servidor da competição `ABServer.jar`, por padrão o Agente se conecta em `127.0.0.1`. O servidor suporta apenas 4 Agentes conectados.

    Rodar `ClientNaiveAgent` na fase `1`
    ```
    java -jar ABServer.jar // subir o servidor, caso não tenha nenhum em pé.
    java -jar ABSoftware.jar -nasc 1
    ```

- **Standalone**

    Rodar `NaiveAgent` na fase `1`
    ```
    java -jar ABSoftware.jar -na 1
    ````

## Uso do Application Cache
Como o site da Aplicação do Angry Brids do Chrome `chrome.angrybirds.com` foi descontinuado, rodar o projeto acabou sendo impossível sem a cópia do **Application Cache** para uso offline do Plugin.
A cópia do mesmo se encontra [aqui](https://github.com/GeorgeJegon/AngryBirds-AI/blob/master/plugin/FallBackChrome/Application%20Cache.zip) , tendo essa pasta em mãos precisamos sobreescrever as mesma pasta dentro dos arquivos do Google Chrome, para isso é necessário parar todos os processos do Chrome e localizar a pasta `Default` a qual contém as informações de cache de aplicação.

Depois te der sobreescrevido a pasta, é só abrir uma janela do Chrome sem estar conectado a Internet (para que o Chrome não tente atualizar seu cache de aplicação), e acessar novamente a url do Plugin, a tela do jogo irá aparecer. E com isso podemos executar novamente o Agente e jogar.

### Localização da Pasta Default
#### Windows XP
`C:\Documents and Settings\%USERNAME%\Local Settings\Application Data\Google\Chrome\User Data\Default`
#### Windows 10/8/7/Vista
`C:\Users\%USERNAME%\AppData\Local\Google\Chrome\User Data\Default`
#### Linux
`~/.config/google-chrome/Default`
#### Mac OS X
`~/Library/Application Support/Google/Chrome/Default`

