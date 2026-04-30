package com.chamaflow.data.models

data class Chama(
    val id: String = "", val name: String = "", val description: String = "",
    val goal: String = "", val logoUrl: String? = null,
    val contributionAmount: Double = 0.0, val penaltyAmount: Double = 0.0,
    val joiningFee: Double = 0.0, val loanInterestRate: Double = 0.10,
    val meetingFrequency: MeetingFrequency = MeetingFrequency.MONTHLY,
    val totalBalance: Double = 0.0, val memberCount: Int = 0, val createdAt: Long = 0L,
    val inviteCode: String = "",
    val joinRequests: List<String> = emptyList(),
    val memberIds: List<String> = emptyList(),
    val createdBy: String = ""
)

enum class MeetingFrequency { WEEKLY, BIWEEKLY, MONTHLY }

data class Member(
    val id: String = "", val chamaId: String = "", val userId: String = "",
    val fullName: String = "", val phoneNumber: String = "", val email: String = "",
    val nationalId: String = "", val joinDate: String = "",
    val role: MemberRole = MemberRole.MEMBER, val status: MemberStatus = MemberStatus.ACTIVE,
    val totalContributions: Double = 0.0, val loanBalance: Double = 0.0,
    val penaltiesOwed: Double = 0.0, val avatarUrl: String? = null,
    val welfareBalance: Double = 0.0
)

enum class MemberRole { ADMIN, TREASURER, MEMBER }
enum class MemberStatus { ACTIVE, INACTIVE }

data class Contribution(
    val id: String = "", val memberId: String = "", val memberName: String = "",
    val amount: Double = 0.0, val date: String = "", val month: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.MPESA,
    val notes: String = "", val status: ContributionStatus = ContributionStatus.PAID
)

enum class PaymentMethod { MPESA, CASH, BANK_TRANSFER }
enum class ContributionStatus { PAID, UNPAID, PARTIAL, OVERDUE }

data class Loan(
    val id: String = "", val memberId: String = "", val memberName: String = "",
    val amount: Double = 0.0, val interestRate: Double = 0.10,
    val repaymentPeriodMonths: Int = 3, val monthlyInstallment: Double = 0.0,
    val totalRepayable: Double = 0.0, val amountPaid: Double = 0.0,
    val remainingBalance: Double = 0.0, val dueDate: String = "",
    val disbursedDate: String = "", val status: LoanStatus = LoanStatus.PENDING
)

enum class LoanStatus { PENDING, ACTIVE, OVERDUE, REPAID, REJECTED }

data class Penalty(
    val id: String = "", val memberId: String = "", val memberName: String = "",
    val reason: PenaltyReason = PenaltyReason.LATE_CONTRIBUTION,
    val amount: Double = 0.0, val dateIssued: String = "",
    val status: PenaltyStatus = PenaltyStatus.UNPAID
)

enum class PenaltyReason { LATE_CONTRIBUTION, MISSED_MEETING, LATE_LOAN_REPAYMENT, MISSED_EVENT }
enum class PenaltyStatus { UNPAID, PAID }

data class Meeting(
    val id: String = "", val chamaId: String = "", val title: String = "",
    val date: String = "", val time: String = "", val venue: String = "",
    val agenda: String = "", val notes: String = "", val decisions: String = "",
    val attendees: List<String> = emptyList(),
    val status: MeetingStatus = MeetingStatus.UPCOMING
)

enum class MeetingStatus { UPCOMING, COMPLETED, CANCELLED }

data class Investment(
    val id: String = "",
    val chamaId: String = "",
    val title: String = "",
    val description: String = "",
    val amountInvested: Double = 0.0,
    val dateInvested: String = "",
    val expectedReturn: Double = 0.0,
    val currentValuation: Double = 0.0,
    val status: InvestmentStatus = InvestmentStatus.ACTIVE,
    val totalDividendsDistributed: Double = 0.0
)

enum class InvestmentStatus { ACTIVE, EXITED, LOSS }

data class DividendDistribution(
    val id: String = "",
    val chamaId: String = "",
    val investmentId: String = "",
    val investmentTitle: String = "",
    val totalAmount: Double = 0.0,
    val dateDistributed: String = "",
    val memberPayouts: List<MemberPayout> = emptyList()
)

data class MemberPayout(
    val memberId: String = "",
    val memberName: String = "",
    val contributionPercentage: Double = 0.0,
    val amount: Double = 0.0
)

data class MerryGoRound(
    val id: String = "",
    val chamaId: String = "",
    val amountPerMember: Double = 0.0,
    val totalPool: Double = 0.0,
    val startDate: String = "",
    val frequency: MeetingFrequency = MeetingFrequency.MONTHLY,
    val rotationOrder: List<String> = emptyList(), // List of memberIds
    val currentIndex: Int = 0,
    val status: MerryGoRoundStatus = MerryGoRoundStatus.ACTIVE
)

enum class MerryGoRoundStatus { ACTIVE, COMPLETED, PAUSED }

data class MerryGoRoundPayout(
    val id: String = "",
    val merryGoRoundId: String = "",
    val memberId: String = "",
    val memberName: String = "",
    val amount: Double = 0.0,
    val datePaid: String = "",
    val month: String = ""
)

data class WelfareFund(
    val id: String = "",
    val chamaId: String = "",
    val totalBalance: Double = 0.0,
    val contributionAmount: Double = 0.0,
    val rules: String = ""
)

data class WelfareContribution(
    val id: String = "",
    val memberId: String = "",
    val memberName: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val month: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.MPESA
)

data class WelfareDisbursement(
    val id: String = "",
    val memberId: String = "",
    val memberName: String = "",
    val amount: Double = 0.0,
    val reason: String = "",
    val category: WelfareCategory = WelfareCategory.EMERGENCY,
    val date: String = ""
)

enum class WelfareCategory { EMERGENCY, FUNERAL, MEDICAL, OTHER }

data class DashboardStats(
    val totalMembers: Int = 0, val totalContributions: Double = 0.0,
    val totalLoansIssued: Double = 0.0, val totalLoanRepayments: Double = 0.0,
    val totalPenaltiesCollected: Double = 0.0, val currentGroupBalance: Double = 0.0,
    val activeLoans: Int = 0, val overdueLoans: Int = 0,
    val upcomingMeeting: Meeting? = null,
    val recentContributions: List<Contribution> = emptyList(),
    val welfareBalance: Double = 0.0
)
