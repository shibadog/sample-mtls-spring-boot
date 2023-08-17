Feature: Request it for the time being

    Background:
        Given url "http://localhost:8081"

    Scenario: Check runnning
        When method get
        Then status 200
        * assert response == "OK"
        * print response
