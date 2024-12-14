package com.example.hook.common.exception
class BlankFieldsException : Throwable("All fields must be filled.")
class InvalidEmailFormatException : Throwable("Email format is not valid.")
class InvalidPasswordException : Throwable("Password must be at least 6 characters long.")
class WeakPasswordException : Throwable("Password must include uppercase, lowercase, and digits.")
class InvalidPhoneNumberException : Throwable("Phone number format is invalid.")
class UsernameTakenException : Throwable("Username is already taken.")
class EmailTakenException : Throwable("Email is already registered.")
class PhoneNumberRegisteredException : Throwable("Phone number is already registered.")
class FailedLinkEmailCredentialException : Throwable("Failed to link email credential")
class FailedPasswordResetException : Throwable( "Failed to reset password")
class FailedToGetIdTokenException : Throwable("Failed to get id Token")
class FailedToSaveUserException : Throwable("Failed to save user data.")
class FailedToSignInWithPhoneNumberException : Throwable("Failed to sign up with phone number")
class IncorrectPasswordException : Throwable("Incorrect password. Please try again.")
class UnexpectedErrorException : Throwable("Unexpected error occurred.")
class UnregisteredUserException : Throwable("No account found with this email, please register first.")
class UserNotLoggedInException : Throwable("No user is currently logged in.")
class FailedAuthenticationException : Throwable("Authentication failed")
class GoogleSignInFailedException : Throwable("Google sign-in failed")
class FacebookSignInFailedException : Throwable("Facebook sign-in failed")
class FacebookLoginCancelledException : Throwable("Facebook login cancelled")
class VerificationFailedException : Throwable("Verification failed")
class RegistrationFailedException : Throwable("Failed to register user.")
