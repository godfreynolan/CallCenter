@callcenters
Feature: Call Centers
	I want to be able to view my call centers
  as a logged in user
	so I can update the status of the users 

Background:
  Given I have logged in successfully

@success
  Scenario: Change status of User
   	Given I am on the "Call Centers" screen
    When I touch the "CallCenterPrem@xdp.broadsoft.com" text
    Then I am on the "Call Center Details" screen
    Then I press view with id "agentStatusButton" 
    Then I change the status
    Then I see the status has changed

   