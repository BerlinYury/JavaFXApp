# Клиент-серверное приложение. Чат.

## Сервер:
1. Создаёт точку подключения.
2. Обрабатывает регистрацию клиента. Создаёт и хранит список Зарегистрированных клиентов. 
3. Обрабатывает аутентификацию клиента. Создаёт список авторизованных клиентов. После успешной аутентификации добавляет нового участника в данный список.
4. Принимает и маршрутизирует сообщения от клиентов.
5. Обрабатывает отключение клиента

## Клиент:
1. Устанавливает соединения с сервером
2. Отправляет на сервер авторизационные данные.
3. Отправляет сообщения другим клиентам 
4. Обрабатывает размещение входящих и исходящих сообщений в визуальной части программы.
5. Обрабатывает служебные сообщения от сервера
6. Обрабатывает смену авторизации клиента.
7. Обрабатывает отключение от сервера
    
## Протокол взаимодействия:
Запрос к серверу:
1. **/end**- команда отключения клиента от сервера. Закрывает соединение. Клиент удаляется из списка авторизованных.
2. **/auth**- команда для аутентификации клиента. Приходит с логином и паролем клиента. "/auth l0 p0"
3. **/changes**-команда для передачи сообщения об изменении списка авторизованных клиентов. Приходит вместе со списком клиенов. используется для отображения списка клиентов на кнопках выбора получателясообщения. "/changes [nick0, nick1]"
4. **/to**-команда для маршрутизации сообщения выбранному поользователю. "/to nick0 msg"
5. **/toAll**-команда для маршрутизации сообщения всем пользователям."/toAll msg"

Ответ от сервера клиенту:
6. **/authOK**- сообщение о том, что клиент прошёл аутентификацию успешно.
7. **/authFailed**-сообщение о том, что клиент не прошёл аутентификацию, по причине того, что поля password, или login были заполнены не верно.
8. **/authNickBusy**-сообщение о том, что аутентификационные данные заполненные клиентом, уже были использованы.

