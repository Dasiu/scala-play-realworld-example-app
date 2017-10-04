package core.users.services

import commons.exceptions.ValidationException
import commons.validations.{Failure, Success}
import core.authentication.api.{NewSecurityUser, SecurityUserCreator}
import core.users.models.{User, UserId, UserRegistration}
import core.users.services.api.UserCreator
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext.Implicits.global

private[users] class UserRegistrationService(userRegistrationValidator: UserRegistrationValidator,
                                             securityUserCreator: SecurityUserCreator,
                                             userCreator: UserCreator) {

  def register(userRegistration: UserRegistration): DBIO[User] = {
    userRegistrationValidator.validate(userRegistration) match {
      case Success => doRegister(userRegistration)
      case Failure(violatedConstraints) => throw new ValidationException(violatedConstraints)
    }
  }

  private def doRegister(userRegistration: UserRegistration) = {
    val newSecurityUser = NewSecurityUser(userRegistration.username, userRegistration.password)
    userCreator.create(User(UserId(-1), userRegistration.username, userRegistration.email))
      .zip(securityUserCreator.create(newSecurityUser))
      .map(_._1)
  }
}



