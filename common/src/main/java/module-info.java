
module com.ongres.scram.common {
  requires java.security.sasl;

  exports com.ongres.scram.common;
  exports com.ongres.scram.common.stringprep;
  exports com.ongres.scram.common.message;
  exports com.ongres.scram.common.gssapi;
  exports com.ongres.scram.common.exception;
  exports com.ongres.scram.common.util;
}
