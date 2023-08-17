Feature: Request it for the time being

    Background:
        Given url "http://localhost:8080"

    Scenario: Check running
        Given path '/test'
        When method get
        Then status 200
        * assert response == "test OK"
        * print response
