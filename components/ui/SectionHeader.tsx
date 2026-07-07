export function SectionHeader({
  eyebrow,
  title,
  description,
}: {
  eyebrow?: string;
  title: string;
  description?: string;
}) {
  return (
    <div className="mb-6">
      {eyebrow && (
        <p className="text-xs font-medium uppercase tracking-[0.18em] text-primary/80 mb-2">
          {eyebrow}
        </p>
      )}
      <h1 className="font-display text-2xl font-semibold text-white">{title}</h1>
      {description && <p className="text-sm text-white/50 mt-1.5 max-w-xl">{description}</p>}
    </div>
  );
}
