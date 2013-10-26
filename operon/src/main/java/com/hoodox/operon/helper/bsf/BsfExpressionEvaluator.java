package com.hoodox.operon.helper.bsf;

import java.util.Iterator;
import java.util.Map;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;

import com.hoodox.commons.exceptions.BaseSystemException;
import com.hoodox.commons.exceptions.ErrorCode;
import com.hoodox.operon.exceptions.EvaluationException;
import com.hoodox.operon.exceptions.SetAttributeException;
import com.hoodox.operon.helper.Const;

public class BsfExpressionEvaluator {
	
	private BSFManager bsf;
	
	@SuppressWarnings("unchecked")
	private Map attributes;

	@SuppressWarnings("unchecked")
	public BsfExpressionEvaluator(Map attributes) {
		if (!BSFManager.isLanguageRegistered("beanshell")) {
			BSFManager.registerScriptingEngine("beanshell", "bsh.util.BeanShellBSFEngine",null);
						
		}
		
		bsf = new BSFManager();
		this.attributes = attributes;
		_declare(this.attributes, bsf);
	}

	/**
	 * Evaluates an integer expression using the local attributes of this case.
	 * <p>
	 * 
	 * @param expression
	 *            the expression to be evaluated.
	 * @return The expression result.
	 * @exception EvaluationException
	 *                if an evaluation error occurs.
	 */
	public int eval(String expression) throws EvaluationException {
		try {
			Object result = bsf.eval("beanshell", "Operon", 0, 0, expression);
			if (result == null) {
				return 0;
			} else if (result instanceof Number) {
				return ((Number) result).intValue();
				
			} else if (result instanceof Boolean) {
				return ((Boolean) result).booleanValue() ? 1 : 0;
				
			} else {
				BaseSystemException ex = new EvaluationException(new ErrorCode(
						Const.ERROR_CODE_bsf_evaulation_error),
						"result " + result + " is not a number or boolean.");
				throw ex;				
			}
			
		} catch (BSFException e) {
			BaseSystemException ex = new EvaluationException(new ErrorCode(
					Const.ERROR_CODE_bsf_evaulation_error),
					"Error in the expression " + expression + " : " + e.getMessage(), e );
			throw ex;				

		}
	}

	/**
	 * Declares all the attributes to be used at expression evaluation.
	 * <p>
	 * 
	 * @param attributes
	 *            the attributes to be declared as a map (<code>String</code>,
	 *            <code>Object</code>) of variables names (as used in edge
	 *            weight expressions) and Java objects. The Java objects should
	 *            be understandable by the underlying BSF engine being used.
	 * @exception SetAttributeException
	 *                if the underlying expression evaluation system has
	 *                problems setting an attribute.
	 */
	@SuppressWarnings("unchecked")
	private static void  _declare(Map attributes, BSFManager bsf) {
		if (attributes != null) {
			Iterator it = attributes.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry attribute = (Map.Entry) it.next();
				_declare((String) attribute.getKey(), attribute.getValue(), bsf);
			}
		}
	}

	/**
	 * Declares an attribute to be used at expression evaluation.
	 * <p>
	 * 
	 * @param id
	 *            the attribute identifier.
	 * @param value
	 *            a Java object with the attribute value. This object should be
	 *            understandable by the underlying BSF engine being used.
	 * @exception SetAttributeException
	 *                if the underlying expression evaluation system has
	 *                problems setting an attribute.
	 */
	private static void _declare(String id, Object value, BSFManager bsf) throws SetAttributeException {
		try {
			bsf.declareBean(id, value, value.getClass());
			//attributes.put(id, value);
		} catch (BSFException e) {
			BaseSystemException ex = new EvaluationException(new ErrorCode(
					Const.ERROR_CODE_bsf_evaulation_error),
					"Could not set variable " + id + " : " + e.getMessage(), e );
			throw ex;				
			
		}
	}

}
