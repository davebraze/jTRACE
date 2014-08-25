//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.4-b18-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.08.25 at 01:29:02 EDT 
//


package edu.uconn.psy.jtrace.parser;


/**
 * Java content class for jtPhonologyType complex type.
 * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at file:/Users/tedstrauss/SVN/jtrace/trunk/src/edu/uconn/psy/jtrace/jTRACESchema.xsd line 1051)
 * <p>
 * <pre>
 * &lt;complexType name="jtPhonologyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="languageName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="phonemes">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="phoneme" maxOccurs="unbounded">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="symbol" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="features">
 *                               &lt;restriction base="{http://xml.netbeans.org/examples/targetNS}listBaseType">
 *                                 &lt;minLength value="1"/>
 *                                 &lt;maxLength value="1000"/>
 *                               &lt;/restriction>
 *                             &lt;/element>
 *                             &lt;element name="durationScalar" minOccurs="0">
 *                               &lt;restriction base="{http://xml.netbeans.org/examples/targetNS}listBaseType">
 *                                 &lt;minLength value="1"/>
 *                                 &lt;maxLength value="1000"/>
 *                               &lt;/restriction>
 *                             &lt;/element>
 *                             &lt;element name="allophonicRelations" minOccurs="0">
 *                               &lt;restriction base="{http://xml.netbeans.org/examples/targetNS}listBaseTypeBool">
 *                                 &lt;minLength value="1"/>
 *                                 &lt;maxLength value="1000"/>
 *                               &lt;/restriction>
 *                             &lt;/element>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 */
public interface JtPhonologyType {


    /**
     * Gets the value of the languageName property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String}
     */
    java.lang.String getLanguageName();

    /**
     * Sets the value of the languageName property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String}
     */
    void setLanguageName(java.lang.String value);

    /**
     * Gets the value of the phonemes property.
     * 
     * @return
     *     possible object is
     *     {@link edu.uconn.psy.jtrace.parser.JtPhonologyType.PhonemesType}
     */
    edu.uconn.psy.jtrace.parser.JtPhonologyType.PhonemesType getPhonemes();

    /**
     * Sets the value of the phonemes property.
     * 
     * @param value
     *     allowed object is
     *     {@link edu.uconn.psy.jtrace.parser.JtPhonologyType.PhonemesType}
     */
    void setPhonemes(edu.uconn.psy.jtrace.parser.JtPhonologyType.PhonemesType value);


    /**
     * Java content class for anonymous complex type.
     * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at file:/Users/tedstrauss/SVN/jtrace/trunk/src/edu/uconn/psy/jtrace/jTRACESchema.xsd line 1055)
     * <p>
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="phoneme" maxOccurs="unbounded">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="symbol" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="features">
     *                     &lt;restriction base="{http://xml.netbeans.org/examples/targetNS}listBaseType">
     *                       &lt;minLength value="1"/>
     *                       &lt;maxLength value="1000"/>
     *                     &lt;/restriction>
     *                   &lt;/element>
     *                   &lt;element name="durationScalar" minOccurs="0">
     *                     &lt;restriction base="{http://xml.netbeans.org/examples/targetNS}listBaseType">
     *                       &lt;minLength value="1"/>
     *                       &lt;maxLength value="1000"/>
     *                     &lt;/restriction>
     *                   &lt;/element>
     *                   &lt;element name="allophonicRelations" minOccurs="0">
     *                     &lt;restriction base="{http://xml.netbeans.org/examples/targetNS}listBaseTypeBool">
     *                       &lt;minLength value="1"/>
     *                       &lt;maxLength value="1000"/>
     *                     &lt;/restriction>
     *                   &lt;/element>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     */
    public interface PhonemesType {


        /**
         * Gets the value of the Phoneme property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the Phoneme property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getPhoneme().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link edu.uconn.psy.jtrace.parser.JtPhonologyType.PhonemesType.PhonemeType}
         * 
         */
        java.util.List getPhoneme();


        /**
         * Java content class for anonymous complex type.
         * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at file:/Users/tedstrauss/SVN/jtrace/trunk/src/edu/uconn/psy/jtrace/jTRACESchema.xsd line 1058)
         * <p>
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence>
         *         &lt;element name="symbol" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="features">
         *           &lt;restriction base="{http://xml.netbeans.org/examples/targetNS}listBaseType">
         *             &lt;minLength value="1"/>
         *             &lt;maxLength value="1000"/>
         *           &lt;/restriction>
         *         &lt;/element>
         *         &lt;element name="durationScalar" minOccurs="0">
         *           &lt;restriction base="{http://xml.netbeans.org/examples/targetNS}listBaseType">
         *             &lt;minLength value="1"/>
         *             &lt;maxLength value="1000"/>
         *           &lt;/restriction>
         *         &lt;/element>
         *         &lt;element name="allophonicRelations" minOccurs="0">
         *           &lt;restriction base="{http://xml.netbeans.org/examples/targetNS}listBaseTypeBool">
         *             &lt;minLength value="1"/>
         *             &lt;maxLength value="1000"/>
         *           &lt;/restriction>
         *         &lt;/element>
         *       &lt;/sequence>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         */
        public interface PhonemeType {


            /**
             * Gets the value of the DurationScalar property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the DurationScalar property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getDurationScalar().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link java.math.BigDecimal}
             * 
             */
            java.util.List getDurationScalar();

            /**
             * Gets the value of the AllophonicRelations property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the AllophonicRelations property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getAllophonicRelations().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * boolean
             * 
             */
            java.util.List getAllophonicRelations();

            /**
             * Gets the value of the symbol property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String}
             */
            java.lang.String getSymbol();

            /**
             * Sets the value of the symbol property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String}
             */
            void setSymbol(java.lang.String value);

            /**
             * Gets the value of the Features property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the Features property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getFeatures().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link java.math.BigDecimal}
             * 
             */
            java.util.List getFeatures();

        }

    }

}