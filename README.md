# S-data

## Формат представления данных

При создании смотрел на XML с его тегами, атрибутами и внутренними элементами. Теги стали называться идентификаторами и представляться в виде строк. Атрибуты стали модификаторами  и представляться в виде строк или чисел (изначально предполагались только строки, но было решено, что выражать числа строками слишком громоздко). Внутренние элементы стали списком объектов.

Изначально предполагалась конструкция вида:
(Ident Mod* (Mod* (...)))

С разделением на модификаторы объекта как целого и модификаторов общих для всех частей этого объекта (левый и правый Mod соответственно). Однако от такой структуры было решено отказаться из-за громоздкости и скорее будут распространены  (Ident Mod* ((...))) т.к. при описании сложных объектов универсальные свойства маловероятны. ТАкже это бы сильно усложнило поиск и модификацию.

Модификатор из себя представляет либо просто идентификатор, либо пару из него и значения (идентификатора или числа).

В итоге пришло к такой структуре (Ident Mod* (...)) Где корневой элемент  в качестве Идентификатора имеет строку “ROOT”, а в качестве значений могут выступать другая структура, строка или число. 

### Синтаксис описания данных в БНФ:

    RootExpr ::= '(' '“ROOT”' Modificator* '(' Value* ')' ')'
    Expr ::= '(' Ident Modificator* '(' Value* ')' ')'
    Value ::= Expr | String | Number
    Ident ::= String
    Modificator ::=  '(' Ident (Ident | Number)? ')'



## Язык навигации и простых запросов

Для этого ориентировался на XPath в краткой форме, сначала рассматривалось с точки зрения чистого языка навигации. 
Есть опциональная часть Premis в которой описываются свойства запроса. К свойствам относятся например версия языка (Представим что у него будет не одна версия), модификация вывода (например всегда выводить элемент со всем путем до него). Далее идут описания частей списка с возможностью добавления модификаторов. Эти модификаторы могут описывать аналог Осей, иметь указание на относительность пути.

Части описываются как конкретный идентификатор или Условия поиска. Условия поиска делятся на условия глубины и условия содержания. 

Условия глубины указывают на каком промежутке от текущего узла ведется поиск и состоят из верхней и нижней границы. Верхняя показывает начальное расстояние и при меньше либо равно нулю означает от внутренних элементов текущего узла; Нижняя показывает конечное расстояние и при равенстве нулю означает до внутренних элементов текущего узла включительно, при меньше нуля поиск будет до конца (ни нада так). Т.е. при условиях ((0)(0)) поиск будет вестись только среди внутренних элементов текущего узла. 
Условия содержания логически представляют из себя структуру вида: (... & … & …) \/ (... & …) \/ … состоящую из одиночных условий.

Одиночные условия Состоят из списка возможных идентификаторов и возможно списка условий модификаторов.

Если список модификаторов пустой, то ограничений не налагается.

Условие модификатора может состоять из идентификатора - требуется модификатор с таким идентификатором,  либо еще ограничение на значение (конкретное или промежуток для чисел)

### Синтаксис описания пути в БНФ:

    Path ::= '(' Premis? PathPart* ')'
    Premis ::= '(' '"ROOT"' Modificator+ ')'
    PathPart ::= '(' ( Ident | SearchConditions ) Modificator* ')'
    SearchConditions ::= '(' SearchDepth JoinedConditions ')'
    SearchDepth ::= '('StartDepth EndDepth')'
    /*Empty or <=0 StartDepth means from current cursor*/
    StartDepth ::= '(' (Number | Ident)? ')'
    /*Empty or <0 StartDepth means till the bottom*/
    EndDepth ::= '(' (Number | Ident)?')'
    /*Several Search Condition is connected as OR*/
    JoinedConditions ::= SearchCondition+ 
    /*If no conditions on Ident, then Ident condition would be ()*/ 
    SearchCondition ::= '(' IdentCondition ModConditions? ')'
    IdentCondition ::= '('Ident*')'
    ModConditions ::= '('ModCondition*')'
    ModCondition ::= '(' Ident (Ident | Number | Number Number)? ')'
    Ident ::= String
    Modificator ::=  '(' Ident (Ident | Number)? ')'


## Язык  описания схемы

При составлении держал в уме XML Schema, однако решил не использовать различные типы, а просто заменить приспособить оригинальную схему хранения данных заменив идентификаторы и модификаторы списками обязательных возможных и невозможных их значений.

Схема состоит из трех частей:

1. GlobalIdentRestrictions - описывает корневые идентификаторы
- GlobalPresentIdents - Идентификаторы которые должны быть в корне
- GlobalPossibleIdents  - Идентификаторы которые могут быть в схеме
- GlobalNonPresentIdents - Идентификаторы которых не должно быть в схеме

Приоритет: GlobalPresentIdents > GlobalPossibleIdents > GlobalNonPresentIdents 

2. GlobalModsRestrictions  - описывает ограничения на связь между идентификатором и модификаторами:
- PresentMods  - Модификаторы которые должны быть при идентификаторе
- PossibleMods  - Модификаторы которые могут быть при идентификаторе
- NonPresentMods  - Модификаторы которых не должно быть при идентификаторе

Приоритет: PresentMods > PossibleMods  > NonPresentMods

3. RootSchema - более конкретизированное описание схем. В нем Можно накладывать дополнительные ограничения на связь между модификаторами и идентификаторами. При этом локальные ограничения распространяются только на текущий уровень и имеют более высокий приоритет по сравнению с глобальными.

### Синтаксис описания схемы в БНФ форме:

    FullSchema ::= '('GlobalIdentRestrictions GlobalModsRestrictions RootSchema ')'
    RootSchema ::= '(' '(' '“ROOT”' ModsRestriction?')' ('(' Schema* ')') ? ')'
    Schema ::= '(' SchemaIdent '(' SchemaValue* ')' ')'
    /*To undersatnd current instance - see the type of first element*/
    SchemaValue ::= Schema | SchemaIdent
    /*Local restrictions have higher priority than global*/
    SchemaIdent ::= '(' (IdentsRestrictions | Ident) ModsRestriction? ')'
    IdentsRestrictions ::= '('PresentIdents PossibleIdents NonPresentIdents  ')'
    PossibleIdents ::= '('ShemaValue*')'
    PresentIdents ::= '('ShemaValue*')'
    NonPresentIdents ::= '('ShemaValue*')'
    GlobalIdentRestrictions ::= ( '('GlobalPresentIdents GlobalPossibleIdents GlobalNonPresentIdents')' ) | ( '(' ')' )
    GlobalPossibleIdents ::= '('Ident*')'
    GlobalPresentIdents ::= '('Ident*')'
    GlobalNonPresentIdents ::= '('Ident*')'
    GlobalModsRestrictions ::= '(' IdentModsRestriction* ')'
    IdentModsRestriction ::= '(' Ident ModsRestriction ')'
    /*PresentMods - have to be;  PossibleMods - could be; NonPresentMods - couldn't be*/
    ModsRestriction ::= PresentMods PossibleMods NonPresentMods
    PossibleMods ::= '(' ModificatorName* ')'
    PresentMods ::= '(' ModificatorName* ')'
    NonPresentMods ::= '(' ModificatorName* ')'
    ModificatorName ::= Ident
    Ident ::= String

[Для удобства просмотра бнф формы](https://www.bottlecaps.de/rr/ui)
