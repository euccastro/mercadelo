# O mercadelo

O mercadelo (mercado dos elos) é um sistema de intercâmbio que visa aproveitar os vínculos de confiança entre pessoas para conectar potencialidades com necessidades além desses vencelhos imediatos.

Por exemplo, supom que o sistema sabe (porque lho dixestes ti) que conheces e confias em Manuel, e (porque lho dixo Manuel) que Manuel conhece e confia em Joana, e que Joana conhece e confia em Antom, o qual conhece e confia em Clara.  Ti tés ũa taberna e fai-che falta que ũa arquitecta che assine um projecto para ũa solicitude de reforma para o local.  Pero ti nom sabes nem que Clara existe, moito menos que é arquitecta.  Ainda que lhe perguntasses a Manuel, el tampouco conhece a Clara.  Tampouco dispós neste momento dos euros para pagar esse trabalho, nem sabes que existe essa relaçom indirecta de confiança com Clara.

Mais se todas tedes conta no mercadelo, o sistema pode-che facilitar encontrar a Clara e ajudar-vos a que cheguedes a um acordo polo qual ela che fai esse trabalho, e ti pagas-lhe nom com euros, mais com creto que serve para comer e beber na tua taberna.

Clara pode gastar esse creto directamente na tua taberna, e nesse caso isto seria como um troco.  Mais Clara tamém pode usar esse creto para pagar-lhe a um mecânico que queira comer e beber na tua taberna, ou a um advogado que queira pagar-lhe a um fontaneiro que queira comprar-lhe ovos a alguém que queira comer e beber na tua taberna, etc.  Clara nom tém que gastar tempo e trabalho em encontrar essa cadea de necessidades e capacidades; para isso é que está o mercadelo.

Para automatizar estas funçons, o mercadelo é mormente um sistema informatizado.  Será possível operar de forma mixta (por exemplo, imprimir bilhetes ou acunhar moedas para usar em feiras), pero para compreender o sistema é melhor explicar o funcionamento normal, que requere usar computador ou telemóvel (o qual, aliás, tamém se pode usar em feiras).

## Objectivos

O objectivo máis imediato é dinamizar as economias locais, empoderar às participantes (tanto individuais quanto colectivas) para que desenvolvam as suas capacidades e satisfagam as suas necessidades, ponhendo ũas em contacto coas outras.

O objectivo último é que ninguém tenha que ter medo de algum dia nom ser capaz de satisfazer algũa necessidade básica.  Nom digo que o mercadelo poda garantir isto, apenas que a motivaçom fundamental do projecto é ajudar nisso.

## Estado do projecto

Despois [dum par de anos de dar-lhe voltas](https://github.com/euccastro/mercadelo/blob/master/doc/apresentaçom.md#máis) na cabeça, estou [começando](https://github.com/euccastro/mercadelo/blob/master/test/mercadelo/core_test.clj) a [programar](https://github.com/euccastro/mercadelo/blob/master/src/mercadelo/core.clj) o sistema.  Suponho que pode levar de seis meses até um ano ter um prototipo pronto para provar.

O objectivo desta descripçom é ir dando o projecto a conhecer e ir conhecendo opinions, sobretodo da gente que acho que puider estar máis interessada em dar-lhe uso.

## Filosofia

O mercadelo nom pretende substituír ou competir cos valores e costumes da gratuidade e do voluntariado.  Trata é de facilitar intercâmbios que a escassez dos euros dificulta.

O mercadelo trata a moeda como um _medio_ de intercâmbio, nom como um _objecto_ de intercâmbio em si.  Isto pode parecer de sentido comum, mais [nom é](http://cadtm.org/spip.php?page=imprimer&id_article=9958) como funcionam as moedas emitidas por bancos.

Como unidade de medida, a moeda nom pode "faltar" máis do que faltem necessidades que cubrir e capacidade para cubrí-las.

## Como furrula

### Resumo

O mercadelo é um sistema monetário no que cada participante, que pode ser ũa pessoa ou coletivo, emite a sua própria moeda, sem límite, e decide quanta moeda quer aceitar de cada ũa das outras participantes.

### Unidade de valor

Todas as moedas valem o mesmo.  Como veremos, isto é crucialmente importante para que o sistema poida encontrar cadeas de pagamento como a descrita acima.

À unidade de valor chamamos-lhe "elo".  O valor dum elo podemo-lo definir de vários jeitos.  Um especialmente simple e efectivo seria dizer que um elo val o mesmo que um euro.  O que é dizer, a gente deveria pôr os mesmos preços aos seus produtos e serviços em elos que em euros.  Outras opçons som possíveis: dar-lhe um valor em horas de trabalho, ou com referência a ũa cesta de bens e serviços básicos, etc.  O sistema funciona máis ou menos igual independentemente de qual se escolha.

### Moedas pessoais

Cada participante tém ũa conta, que começa a zero.  Se Joám e Marta estám coas contas a zero, e Marta é fontaneira e Joám tém ũa bilha escaralhada, Joám pode-lhe pagar a Marta em "elos do Joám" para que lha amanhe.  Marta pode aceitar ou recusar os elos do Joám.  Digamos que Marta lhe aceita 30 elos.  Logo a conta de Marta fica com 30 elos-do-Joám e a conta do Joám fica com -30 elos-do-Joám.

No dia seguinte Marta vai-lhe tomar ũas canhas à taberna do Fabiám, e despois uns gin tonics ao pub do Joaquim.  Fabiám aceita-lhe a Marta 6 elos do Joám.  Joaquim nom quer os elos do Joám, mais si os da Marta.  Logo Marta fica com 24 elos do Joám e com -12 elos (o que custárom os gin tonics) dos seus.

#### Porque?

Seria máis simples que todo o mundo tivesse a mesma moeda e portanto só um saldo, positivo ou negativo.  [Esse sistema existe](http://es.wikipedia.org/wiki/Sistema_de_cambio_local), mais infelizmente é fácil abusar del e portanto só funciona em comunidades relativamente pequenas onde hai moita confiança.  Isto poderia ser o caso do Foro Ecolóxico de Ribeira.  Mais ũa moeda pode ser máis util se liga gente que trabalha em diversos sectores.  O mercadelo tenta ligar comunidades diversas que talvez ainda nom se conheçam o suficiente para confiar ilimitadamente entre si.

Um exemplo de abuso seria que o meu cunhado entra no sistema e me paga um milhom de elos.  El fica "arruinado" pero tanto lhe tém porque nom pensava participar de boa fe.  Em princípio, eu podo gastar o meu milhom de elos no que quiger.  Ainda se todas as contas som públicas, pode-se fazer o mesmo menos descaradamente.  Em geral, o sistema nom incentiva que ũa participante recuse o pagamento doutra se lhe parece que está abusando.  Formalmente, mesmo incentiva o contrário.  Basta ũa pouca gente irresponsável, malintencionada, ou simplesmente gamberra para desestabilizar todo o sistema e minar a confiança do resto das participantes.

Hai variantes nas que ponhem um límite de creto para paliar esse problema.  Isto introduz a necessidade dum órgao gestor que decida que límites assignar a cada quem.  Mais sobretodo, isso muda fundamentalmente o carácter da moeda, que agora vira escassa, paralisando intercâmbios.  Hai contramedidas para isto último, pero agravam o problema da gestom e introduzem outros (podemos extendermo-nos nisto na assembleia se hai interesse e dá tempo).

Co sistema de moedas pessoais, o milhom de elos do meu cunhado só me serve na medida em que encontre gente tam incauta como para aceitar os elos dele.  Difícil, tendo em conta que, como veremos a seguir, as contas som públicas.

### Transparência

Todas as contas e movementos som visíveis para todas as participantes.  Hai várias razons para isso:

- Permite a qualquer participante detectar abusos (por exemplo, gente que só consome e, podendo, nunca achega nada).

- Permite a qualquer participante detectar erros de funcionamento, ou assegurar-se de que todo funciona bem.

- Permite a qualquer participante guardar cópias de segurança do estado de todas as contas.  No pior dos casos, se o sistema deixar de funcionar algum dia, sempre poderiam resolver as contas pendentes manualmente.

- Alguém tém que ter a possibilidade de ver o estado das contas para diagnosticar e resolver problemas que poidam surgir, por exemplo, por erros de programaçom.  Se nom todo o mundo pode ver todo, a gente que mantenha o sistema teria acesso privilegiado à informaçom, o qual pode gerar suspeitas e drama desnecessário.

- Garantir a privacidade das contas dentro do sistema faria o desenho e programaçom máis complexas.

Compreendo que hai razons legítimas de privacidade para desejar contas opacas (como tamém argumentos razoáveis no sentido contrário).  Mais acho que neste momento só ũa moeda que funcione com transparência total pode ganhar a confiança da gente.

### Déveda

Cada participante pode gastar tanta da _sua_ moeda como lhe queiram aceitar, mais nom pode ficar a dever ningũa moeda máis que a sua.  Logo, em geral, todas as participantes tenhem um saldo igual ou menor que zero na moeda própria, e um saldo igual ou maior do que zero na do resto da gente.

Nom hai, em princípio, ningum estigma associado a estar "em déveda".  Como a suma de todas as contas é zero, é esperável que em cada momento aproximadamente a metade da gente tenha máis "déveda" que saldos positivos.  Aliás, assi é como funciona a moeda bancária.  A imensa maioria dos euros em circulaçom venhem literalmente da déveda de alguém com um banco.

Mesmo nalguns casos, como pessoas dependentes ou desvalidas que permanentemente necessitam máis da sociedade do que podem achegar, pode ser socialmente aceitado que aumentem constantemente a sua déveda.  O sistema monetário nom determina isto, é questom do que queiram e poidam aceitar as pessoas e comunidades participantes.

### Confiança

Nos exemplos que demos até agora dava-se a entender que oferecer e aceitar moedas é um processo manual.  Isto é um jeito de fazê-lo, mais nom é o máis cómodo nem eficiente, e nom permite ao sistema descubrir cadeas como as descritas no primeiro exemplo.

Se Marta confia no Joám e em que os seus elos lhe serám úteis (bem porque a ela lhe interessa directamente o que o Joám oferece por eles, ou bem porque sabe que sempre os pode gastar na taberna do Fabiám, ou bem porque sabe que em geral é fácil gastar os elos do Joám), logo pode-lhe dizer ao sistema que aceita automáticamente (por exemplo) até 300 elos do Joám.  O sistema pode usar esta informaçom para

- aceitar automáticamente (sem perguntar-lhe antes a Marta) pagamentos em elos-de-Joám até essa quantidade.

- trocar-lhe a Marta elos-da-Marta (ou quaisquer outros que ela tiver) por elos-do-Joám, se isso ajuda a encontrar um pagamento entre outra gente.  Por exemplo, o Fabiám quer ir tomar gin tonics ao pub do Joaquim.  O Fabiám só tém elos seus máis do Joám.  O Joaquim nom quer ningum dos dous, mais si os de Marta.  O sistema passa da conta do Fabiám à de Marta 12 elos-do-Joám, e passa da de Marta à do Joaquim 12 elos-da-Marta.

Isto é beneficioso para Marta e para a comunidade.  Para Marta porque tém máis diversidade de opçons para pagar.  E para a comunidade porque o permisso e informaçom que Marta dá fai possíveis pagamentos que doutro jeito nom se poderiam realizar.

Ainda que aqui vimos um exemplo de cadea de pagamento em dous "passos" (Fabiám->Marta, Marta->Joaquim), para o sistema é igual de fácil encontrar cadeas de pagamento com 3, 4 ou máis "passos", como elos dũa cadea (daí, em parte, o nome da moeda).

### Comunidades

O sistema de aceitaçom automática tamém facilita abrir contas comunitárias.  Por exemplo, o Foro Ecolóxico de Ribeira poderia abrir ũa conta que aceitasse automáticamente até, digamos, 1000 elos de cada ũa das entidades membro.  Os elos do Foro inspirariam máis confiança que os da maioria das agricultoras e artesãs individuais, e portanto aceitaria-os automáticamente máis gente-- mesmo gente sem interesse directo em comprar produtos do Foro pode aceitar essa moeda porque sabe que hai moita outra gente que a aceita.  Logo as produtoras e artesãs poderiam pagar cos seus elos próprios em máis lugares (porque os do Foro funcionariam como intermediários, igual que os da Marta no último exemplo).

Além disso, o Foro pode empregar a confiança que vaia ganhando para aumentar a sua capacidade de endevedar-se para acometer projectos de investimento.  Por exemplo, hai um projecto para criar vacas de raças autóctones, em acordo com comunidades de montes locais, mais fai falta acometer trabalhos de acondicionamento e valado.  O Foro pode pagar parte desse trabalho em elos-do-Foro, que num futuro se irám extinguindo bem em doaçons, bem em pagamento por carne ecológica, coiro para artesanias, etc.

É claro que para isso o Foro tém que convencer às outras participantes no mercadelo de que o projecto fai sentido e de que as participantes no Foro respondem pola moeda emitida e polos riscos do projecto.

Nom todo se poderá pagar com elos, mais no que se puider, é ũa possibilidade interessante para financiar projectos sem juros.

### Centrais de compras (em euros)

Hai gente que tém emprego remunerado em euros e que nom tém tempo ou gana de produzir nada fóra do horário laboral para oferecer no mercadelo, pero que por afinidade ou simpatia quererá colaborar.  Um jeito de fazê-lo seria comprando elos do Foro Ecolóxico (ou de outro colectivo) com euros.  Esses elos seriam gastados no mercadelo normalmente.

O Foro poderia empregar esses quartos para cubrir gastos das participantes que tenham necessariamente que ser pagos em euros, priorizando polos critérios que decidir a assembleia.  A assembleia pode tamém escolher proveedoras conforme aos seus critérios éticos, usar a agregaçom da demanda para negociar melhores preços, ou para que aceitem parte do pagamento em elos-do-Foro.  Outra opçom é, de novo, investí-los em projectos como o descrito acima.

### Outras cousas

O que segue seguramente nom seja necessário para compreender o sistema, mais responde a perguntas que tenho ouvido sobre ela.

#### Mais isto é moi complicado!

Hai sistemas máis simples de moeda social.  Acho que esses sistemas podem ser emulados como um caso especial dentro do mercadelo, e que isto tém vantages sobre criar um sistema simples mais isolado.

Por exemplo, se o Foro imprime elos-do-Foro em forma física, usar esses bilhetes nom é máis complicado do que usar bilhetes de euros.  As produtoras que recibam esses bilhetes e moedas levam-nas ao Foro para que lhas registe no mercadelo.  O foro pode entom pôr esses bilhetes a circular de novo.  As [landras](https://proxectointegralcompostela.wordpress.com/moeda-social/a-landra-moeda-social-do-pic/) do Proxecto Integral Compostela som um tal exemplo de moeda mixta (web máis cheques).

Assemade, quem nom se quiger romper a cabeça com tanta moeda pode operar só cos elos-do-Foro, nunca gastando os seus nem aceitando outros, e entom o sistema nom é máis complicado de usar do que qualquer moeda social.

É claro que usar os elos assi nom tém todas as vantages do mercadelo.  Todavia, a gente que preferir esse uso simplificado da moeda seguiria a beneficiar-se da maior aceitaçom e circulaçom que os elos-do-Foro obteriam polo feito de que outra gente os poida usar para pagar serviços oferecidos por outras pessoas e comunidades.

#### O que fazer coa acumulaçom?

Noutros desenhos de moeda social tenta-se combater explicitamente a acumulaçom de moeda.  Isto é mormente porque nesses modelos a moeda é finita, e portanto quem acumula moeda está a privar a outra gente dela e a reduzir a massa circulante.  Em princípio, num sistema como o mercadelo (para quem estiver familiarizado com temas de moeda social: um sistema de creto mútuo puro) nom hai escassez de moeda.  A moeda cria-se quando fai falta para comerciar.  Havendo necessidades, capacidades e confiança unindo-as, a moeda nunca falta.

#### Unidade de Valor (revisitada)

Se Marta confia no Fabiám, pero menos que no Joám, Marta pode aceitar automáticamente só 50 elos do Fabiám.  O que nom pode é aceitar elos do Fabiám a outro preço.  Tenhem-me comentado isto como ũa desvantage do sistema: que nom permite "expressar" esse tipo de preferências.  E é certo.  Mais se as moedas nom valessem o mesmo o sistema nom poderia resolver cadeas de pagamentos.  E acho que essa capacidade é moito máis importante que a de poder pôr um preço subjectivo à moeda doutra participante.  De feito, isto último serve de pouco e ameaça promover a especulaçom com moedas.

##### Mais como fazer que a gente respeite a unidade de valor?

Acho que o feito que o sistema poda intercambiar qualquer moeda que aceites por qualquer outra desincentiva unidades de valor diferentes à que fixemos como comunidade, fazendo-as pouco prácticas.

Imagina que o Joaquim vai de guai e di que os seus elos valem 10 euros em vez de 1.  Ti nom quererias _ter_ elos-do-Joaquim porque che costárom dez vezes máis e em qualquer momento o sistema pode-chos trocar por elos próprios ou por qualquer outros elos que aceites.

Imagina que o Fabiám di que os seus elos valem 10 céntimos.  Ti nom quererias _aceitar automáticamente_ esses elos, porque o sistema poderia-chos dar a câmbio de tirar-che outros máis "valiosos".

### Máis

Neste [tocho de hai dous anos](https://n-1.cc/discussion/view/1606045/livians-um-sistema-de-credito-mutuo-com-moeda-persoal-i), e sobretodo na discussom que segue, debulham-se máis alguns conceitos que aqui só aparecem bastante resumidos.  Naquel tempo dava-lhe importância demais ao feito de que o sistema fechasse ciclos de déveda (cousa que neste texto do 2015 nem menciono) e ainda que tinha a mesma intuiçom que hoje quanto à motivaçom para ligar cadeas de pagamentos e para a gente unir-se para criar contas colectivas, nom tinha nada claros os mecanismos para estas duas cousas e cometo alguns erros ao tentar explicá-los.

### Comentários?

Escreve qualquer comentário ou pergunta a [euccastro@gmail.com](mailto:euccastro@gmail.com).  Especifica se queres que apareça nesta secçom.  Tentarei responder todos os correios, mais só prometo publicar aqui os que sejam construtivos (por críticos que forem).
