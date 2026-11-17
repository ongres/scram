/*
 * Copyright (c) 2026 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.util;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Set;

/**
 * A certificate that reports its signature algorithm under a different name and OID, the way
 * another security provider would, and delegates everything else to a real certificate.
 */
@SuppressWarnings("deprecation")
final class RenamedSigAlgCertificate extends X509Certificate {
  private static final long serialVersionUID = 1L;

  private final X509Certificate delegate;
  private final String sigAlgName;
  private final String sigAlgOid;

  RenamedSigAlgCertificate(X509Certificate delegate, String sigAlgName, String sigAlgOid) {
    this.delegate = delegate;
    this.sigAlgName = sigAlgName;
    this.sigAlgOid = sigAlgOid;
  }

  @Override
  public String getSigAlgName() {
    return sigAlgName;
  }

  @Override
  public String getSigAlgOID() {
    return sigAlgOid;
  }

  @Override
  public byte[] getSigAlgParams() {
    return delegate.getSigAlgParams();
  }

  @Override
  public byte[] getEncoded() throws CertificateEncodingException {
    return delegate.getEncoded();
  }

  @Override
  public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
    delegate.checkValidity();
  }

  @Override
  public void checkValidity(Date date)
      throws CertificateExpiredException, CertificateNotYetValidException {
    delegate.checkValidity(date);
  }

  @Override
  public int getVersion() {
    return delegate.getVersion();
  }

  @Override
  public BigInteger getSerialNumber() {
    return delegate.getSerialNumber();
  }

  @Override
  public Principal getIssuerDN() {
    return delegate.getIssuerDN();
  }

  @Override
  public Principal getSubjectDN() {
    return delegate.getSubjectDN();
  }

  @Override
  public Date getNotBefore() {
    return delegate.getNotBefore();
  }

  @Override
  public Date getNotAfter() {
    return delegate.getNotAfter();
  }

  @Override
  public byte[] getTBSCertificate() throws CertificateEncodingException {
    return delegate.getTBSCertificate();
  }

  @Override
  public byte[] getSignature() {
    return delegate.getSignature();
  }

  @Override
  public boolean[] getIssuerUniqueID() {
    return delegate.getIssuerUniqueID();
  }

  @Override
  public boolean[] getSubjectUniqueID() {
    return delegate.getSubjectUniqueID();
  }

  @Override
  public boolean[] getKeyUsage() {
    return delegate.getKeyUsage();
  }

  @Override
  public int getBasicConstraints() {
    return delegate.getBasicConstraints();
  }

  @Override
  public void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException,
      InvalidKeyException, NoSuchProviderException, SignatureException {
    delegate.verify(key);
  }

  @Override
  public void verify(PublicKey key, String sigProvider) throws CertificateException,
      NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
    delegate.verify(key, sigProvider);
  }

  @Override
  public String toString() {
    return "RenamedSigAlgCertificate[" + sigAlgName + ", " + sigAlgOid + "]";
  }

  @Override
  public PublicKey getPublicKey() {
    return delegate.getPublicKey();
  }

  @Override
  public boolean hasUnsupportedCriticalExtension() {
    return delegate.hasUnsupportedCriticalExtension();
  }

  @Override
  public Set<String> getCriticalExtensionOIDs() {
    return delegate.getCriticalExtensionOIDs();
  }

  @Override
  public Set<String> getNonCriticalExtensionOIDs() {
    return delegate.getNonCriticalExtensionOIDs();
  }

  @Override
  public byte[] getExtensionValue(String oid) {
    return delegate.getExtensionValue(oid);
  }
}
