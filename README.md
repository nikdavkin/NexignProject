# Комментарии к решению:
1. Не сказано, нужно ли удалять папку reports между решениями или нет, поэтому не удалял, так как в Java это достаточно неприятный процесс(нужно сначала удалить все файлы из папки, а потом только саму папку).
2. Форматирование использовал только для Duration и Cost, так как у остальных колонок размер фиксированный.
3. В условии написано, что в зависимости от вендора, cdr может быть в разном формате, однако все 5000 cdr в файле одного формата(либо я не понял, что имеется ввиду). Поэтому ориентировался именно на этот формат(имеются ввиду substring по конкретным индексам для парсинга строки).
4. Для безлимита написано, что первые 300 минут - 0 рублей. Если пользователь проговорил за расчётный период меньше 300 минут, он всё равно должен заплатить 100 рублей или он ничего не должен платить? В моём решении пользователь в любом случае должен будет заплатить за период, даже если не превысил лимит.
В целом, решение достаточно простое: 
- считываем все строки в HashMap с номером в качестве ключа;
- сортируем мапу по дате(client.getValue().sort(Comparator.comparing(o -> o.substring(4, 18)));
- для каждого Entry в мапе создаём файл с номером абонента и организуем вывод.


Задание:
CDR - сall data record - формат файла, содержащего в себе информацию о времени, стоимости и типа вызова абонента.

В зависимости от вендора, cdr может быть в разном формате, но всегда содержит следующие данные: - номер абонента (все номера сгенерированы. все совпадения случайны) - тип вызова (01 - исходящие, 02 - входящие) - дата и время начала звонка (YYYYMMDDHH24MMSS) - дата и время окончания звонка - тип тарфа (см. ниже)

Вот пример готовой записи cdr:

02, 79876543221, 20230321160455, 20230321163211, 11

Тарифов всего 3:
(06) Безлимит 300: 300 минут - за тарифный период* стоят фиксированную сумму - 100р. Каждая последующая минута - 1р.

*Тарифным периодом будем считать все звонки совершенные в текущей выгрузке.
(03) Поминутный: 1 минута разговора - 1.5 рубля.
(11) Обычный: Входящие - бесплатно, исходящие - первые 100 минут по 0.5р/минута, после по тарифу "поминутный".

Конечная задача будет протарифицировать абонентов: т.е. сгенерировать файл отчета для каждого абонента, содержащий в себе номер, индекс тарифа, все совершенные звонки в календарном порядке с выпиской их стоимости по тарифу (для безлимита первые 300 минут - 0р, а с 301 уже расчет), продолжительность звонка, итоговая сумма списания.

Для реализации функционала разрешается использовать только средства стандартной библиотеки java (JDK 11). Система сборки - maven. Приложение должно содержать рабочий метод main() для запуска генератора отчетов. Отчеты собирать в директорию reports в корневой папке проекта.
