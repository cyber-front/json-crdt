package com.cyberfront.crdt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cyberfront.crdt.operations.AbstractOperation;
import com.cyberfront.crdt.operations.OperationManager;

public class CRDTManager {

	/** The Constant logger. */
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(CRDTManager.class);

	/** The crdt. */
	private LastWriteWins crdt;
	
//	private boolean flag = false;
	
	/**
	 * Gets the crdt.
	 *
	 * @return the crdt
	 */
	protected LastWriteWins getCrdt() {
		if (null == this.crdt) {
			this.crdt = new LastWriteWins();
		}
		return crdt;
	}

	/**
	 * Clear.
	 */
	public void clear() {
		this.getCrdt().clear();
	}

	/**
	 * Checks if is created.
	 *
	 * @return true, if is created
	 */
	public boolean isCreated() {
		return this.getCrdt().isCreated();
	}
	
	/**
	 * Checks if is deleted.
	 *
	 * @return true, if is deleted
	 */
	public boolean isDeleted() {
		return this.getCrdt().isDeleted();
	}
	/**
	 * Deliver.
	 *
	 * @param op the op
	 */
	private void deliver(AbstractOperation op) {
//		assert(this.isFlagged());
//		this.resetFlag();
		this.getCrdt().addOperation(op);
	}

	/**
	 * Cancel.
	 *
	 * @param op the op
	 */
	private void cancel(AbstractOperation op) {
//		assert(this.isFlagged());
//		this.resetFlag();
		this.getCrdt().remOperation(op);
	}

	/**
	 * Deliver.
	 *
	 * @param op the op
	 */
	protected void deliver(OperationManager op) {
		switch(op.getStatus()) {
		case APPROVED:
		case PENDING:
			this.deliver(op.getOperation());
			break;
		case REJECTED:
			this.cancel(op.getOperation());
			break;
		default:
			break;
		}
		
		this.getCrdt().getInvalidOperations().clear();
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseManager#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (null == obj || !(obj instanceof CRDTManager) || !super.equals(obj)) {
			return false;
		}
		
		CRDTManager mgr = (CRDTManager) obj;
		
		return this.getCrdt().equals(mgr.getCrdt());
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseManager#hashCode()
	 */
	@Override
	public int hashCode() {
		return 31 * super.hashCode() + this.getCrdt().hashCode();
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseManager#getSegment()
	 */
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();

		sb.append("\"crdt\":" + (null == this.getCrdt() ? "null" : this.getCrdt().toString()));
		
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return "{" + this.getSegment() + "}";
	}
	
//	public void setFlag() {
//		this.flag = true;
//	}
//
//	public void resetFlag() {
//		this.flag = false;
//	}
//	
//	public boolean isFlagged() {
//		return this.flag;
//	}

}
