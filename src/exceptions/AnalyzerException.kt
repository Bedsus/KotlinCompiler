package exceptions

/**
 * Класс `AnalyzerException` представляет исключения, которые могут быть вызваны
 * по лексическим или синтаксическим ошибкам
 *
 * Положение во входном источнике (лексере) или номер токена (парсера), где
 * произошла ошибка
 */
class AnalyzerException(override val message: String) : Exception()