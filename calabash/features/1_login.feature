@login
Feature: Login feature
	as a user of Call Center app
	I want to be able to log in
	so I can use the app

@success
  Scenario: Enter Login Info
  Given I am on the "Call Queue Manager" screen
    When I enter "gnolanUser1@xdp.broadsoft.com" into the username field
    And I enter "welcome1" into the password field
    And I enter "http://xsp2.xdp.broadsoft.com" into the server field
    And I press the "Done" button
    Then I see "Call Centers" screen

@fail
   Scenario Outline:  Invalid Login
	Given I am on the "Call Queue Manager" screen
    When I enter "" into the username field
    And I enter "<username>" into the username field
    And I enter "" into the password field
    And I enter "<password>" into the password field
    And I enter "" into the server field
    And I enter "<server>" into the server field
    And I press the "Done" button
    Then I see "<screen>" screen
    And I press the "OK" button

    Examples:
    | username                      | password | server                              | screen                               |
    | gnolanUser1@xdp.broadsoft.com | weRWEme1 | http://xsp2.xdp.broadsoft.com       | Login failed: Unauthorized           |
    | gnolanUser1@xdp.broadsoft.com | welcome1 | http://xsp2.xdp.broqweqweadsoft.com | Login failed: Unable to resolve host |
    | gnolanUser1@xdp.broadsoft.com |          | http://xsp2.xdp.broadsoft.com       | Please fill in every field           |

