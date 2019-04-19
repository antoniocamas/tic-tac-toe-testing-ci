============
 Práctica 2
============

Implementacion de Pruebas Automáticas
=====================================

:Autor: Antonio Camas Maestre
:git: https://github.com/antoniocamas/tic-tac-toe-testing
   
.. contents:: Contenidos
	      
.. raw:: pdf

   PageBreak

Introduccion
---------------

Todos las fases de prueba, unitaria, de integración y de sistema contemplan los siguientes escenarios::

  - El primer jugador que pone ficha gana.
  - El primer jugador que pone ficha pierde.
  - Ninguno de los jugadores gana. Hay empate


Pruebas Unitarias con JUnit 4 de la clase Board
------------------------------------------------

El testing de esta clase se ha hecho con unit test en sentido amplio. Es decir, no se han empleado
dobles para sus colabores necesarios. La clase Board usa objetos de la clase de datos Cell.

**BUGS:**

**Método board.getCellsIfWinner(String):** durante la realización de estos tests se ha encontrado un bug en el método *board.getCellsIfWinner(String)*. Las comparaciones *cellValue.equals == this.cells.get(winPos[1]).value* devuelven *false* en situaciones en las que los escenarios requerían un *true*. El bug se debe a un mal uso de operador *==* que en Java comprueba la igualdad de objetos y no meramente la igualdad en el contenido de los objetos. Este problema hacía imposible ejecutar correctamente los tests unitarios. Para solventarlo se ha cambiado el código del sistema bajo test por esta expresion que comprueba la equidad del contenido de dos objetos String *cellValue.equals(this.cells.get(winPos[1]).value)*.


**Método board.checkDraw:** es método devuelve true en situaciones en las que no hay empate. En concreto si la novena pieza hace tres en raya este metodo devuelve un falso *True*. Este problema no sale a la luz en los tests de sistema o de integración porque siempre se llama al método *board.getCellsIfWinner(String)* antes. Mirando la implementación del método la recomendación sería cambiar el nombre a algo como *board.checkFull*, implica que se dejaría la responsabilidad de conocer las reglas del TicTacToe a las capas de abstracción superiores evitando el problema.

BoardTest.java
^^^^^^^^^^^^^^

Prueba los métodos publicos de la clase Board con tests no parametrizados. Hace uso de los *matchers* de la librería AssertJ que facilan la lectura de los tests y ofrecen mensajes de error muy legibles.

La construcción de los escenearios bajo test "Given" tiene en común la creación del tablero que se extrae a un método *Setup*.

Los tests en este fichero prueban los escenarios más comunes pero no ejercitan toda la combinatoria posible de movimentos. Por tanto no tienen mucho código en común que refactorizar y parametrizar.

Mereze la pena sin embargo probar una mayor combinatoria de movimientos y posibilidades de jugadas ganadoras. Estos test irán en otra clase de test esta vez si parametrizada.

Parametrización de las pruebas
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Class Movement
""""""""""""""

En aras de la reutilización de código se crea la clase de apoyo para los test *Movement* que encapsula el jugador, la descripción de un moviento especifico y el resultado de este movimiento en la partida, gana, empata o "no es un movimiento final".

Así pues los casos de pruebas quedarán completamente parametrizados con una lista de movimientos. Se usa en las todas las displinas de pruebas, unitaria, de integración y de sistema.

Class TestScenarios
"""""""""""""""""""

Los escenarios que que se prueban en cada fase son los mismos. Lo que conlleva que la secuencia de movientos y resultado esperado sea la misma. Para no repetir código que crea la clase pura de datos *TestScenarios* que encapsula la lista de objetos *Movement* que definen cada escenario de prueba.


BoardTestParam.java
^^^^^^^^^^^^^^^^^^^

Prueba combinatoria de movimientos y posibilidades de jugadas ganadoras. Todas las jugadas y tanto el testing negativo como el positivo del método *board.getCellsIfWinner(String)* se ejercita en un solo test parametrizado.

Existen escerarios duplicados entre los test de esta clase y los de *BoardTest.java* sin embargo se decide no eliminar la duplicidad al ser los tests poco costosos en ejecución y para poder beneficiarnos de la facilidad comprension y lectura de que otorga la simplidad de los tests no parametrizados, sobre todo en caso de que alguno detectase alguna regresión.


Tets GivenABoard_when_play_getsWinnerRight
""""""""""""""""""""""""""""""""""""""""""

Reciben por parametro los movimientos a realizar y el resultado esperado de *getCellsIfWinner* tras cada moviento.

Así pues una ejecución recibe una lista de movimientos y tras cada moviento el tests hace una aserción probando un escenario.

Para facilitar la legibilidad del test se ha usado una lista de objetos de la mencionada clase *Movement* que describe un escenario en concreto.

Es una cuestión pendiente el mejorar el nombrado de los tests parametrizados. El idioma *@Parameters(name = "{index}: {0})* no aporta legibilidad puesto que los elementos del parametro 0 son *List<Movement>* que no tiene una buena representación ascii cuando se imprimen. Quizas con un mayor conocimiento de Java se hubiera podido solventar. Por ejemplo en otros leguajes como Python hubiera bastado con reimplementar el métido *__repr__* de los objetos *List<Movement>*. Con el objetivo de aportar claridad cada elemento *List<Movement>* tiene un nombre de variable que intenta identificar el escenario que prueba.


Pruebas de integración con Mockito de la clase TicTacToeGame
-------------------------------------------------------------

Las pruebas de integración requieren usar DOCs (Dependent-On Classes) cuando sea posible. Es por esto que clases como *Board* o *Player* que no se deben sustituirá por dobles. La implementacion de *TicTacToeGame* no prevé tampoco que estas clases se puedan sustiur por dobles puesto que se crean dentro del contructor de directamente con *new*. En caso de haber tenido que hacer test unitarios siguiendo una aproximáción cercada al TDD se podría haber anotado la creación de Board con *@Resource* para poder pedir a mockito que use la inyección de dependencias. (https://stackoverflow.com/questions/8995540/mocking-member-variables-of-a-class-using-mockito).

La implementación de estos tests está recogida en la clase *TicTacToeGameTest* y consta de un solo test case parametrizado con una lista de movimientos *List<Movement>* y comprueba usando *verify* de Mockito combinado con *matchers* que se envían por los WebSockets los eventos esperados. Para ellos se han usado dobles de los objetos *Connection*.

Estos tests requieren conociento de la implementación interna de *TicTacToeGame*. Han sido los tests que han requerido más tiempo de implementacion.


Pruebas de sistema de la aplicación
-----------------------------------

Estas pruebas se han implementado usando selenium, instanciando un navegador *Chrome* por cada jugador. Se vuelve a utilizar *List<Movement>* como elemento de entrada a un solo test parametrizado: *TicTacToeWeb_Generic_System_Test*.

Los tests levantan la aplicación antes de ejecutar los test (*@BeforeClass*) y la tira cuando han acabado todos (*@AfterClass*). Así las instancias de los navegadores son creadas (*@Before*) y destruidas (*@After*) especificamente en cada test.


Conclusiones
------------

Las tecnologías de ejecución e implementación de tests están preparadas para ser usadas en las tres fases de pruebas que se estudian aquí.

Así pues el framework de Junit, la parametrización, los matchers, y el lenguaje AssertJ han sido usados en las tres fases de pruebas. Lo que realmente las diferencia es realmente un sentido conceptual: **Qué parte del software está bajo test**.

Me gustaría hacer una sugerencia con respecto al temario de esta asignatura. Echo en falta una guía que ayude a identificar qué scenarios de tests debería cubrir una fase de pruebas dada. Alguna técnica que ayude a análizar las posibles entradas de un objeto bajo test y las posibles salidas, y a hacer una selección de aquellas que cubran con mayor eficiencia y eficacia el código. En esa misma linea para esta práctica los escenarios podrían no haber venido dados siendo el alumno el responsable de encontrarlos.
