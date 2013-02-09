package semper.sil.verifier

/** Describes the outcome of a verification attempt of a SIL program.
  *
  * @author Stefan Heule
  */
sealed trait VerificationResult

/** A successful verification. */
object Success extends VerificationResult

/** A non-successful verification.
  *
  * @param errors The errors encountered during verification.
  */
case class Error(errors: Seq[VerificationError]) extends VerificationResult