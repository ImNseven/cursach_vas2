interface SimilarityBadgeProps {
  score: number;
}

export default function SimilarityBadge({ score }: SimilarityBadgeProps) {
  const percentage = Math.round(score * 100);

  const getColor = () => {
    if (percentage >= 70) return 'bg-green-100 text-green-800 border-green-200';
    if (percentage >= 40) return 'bg-yellow-100 text-yellow-800 border-yellow-200';
    return 'bg-orange-100 text-orange-800 border-orange-200';
  };

  return (
    <div className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold border ${getColor()}`}>
      <div className="w-1.5 h-1.5 rounded-full bg-current opacity-60" />
      {percentage}% совпадение
    </div>
  );
}
